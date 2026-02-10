# 고민순삭 프로젝트 DB 연동 가이드

## 📋 목차
1. [카카오맵 (취업지원 센터)](#1-카카오맵-취업지원-센터)
2. [리뷰 CRUD](#2-리뷰-crud)
3. [최근 민감 키워드 리스트 (관리자)](#3-최근-민감-키워드-리스트-관리자)
4. [최근 활동 내역 (상담사)](#4-최근-활동-내역-상담사)
5. [백엔드 구조 설정](#5-백엔드-구조-설정)
6. [DB 스키마 전체](#6-db-스키마-전체)

---

## 1. 카카오맵 (취업지원 센터)

### 📁 파일 위치
- **프론트엔드**: `frontend/src/pages/user/info/Map.jsx`
- **API 파일 생성**: `frontend/src/api/centerApi.js` (신규 생성 필요)
- **백엔드**: `backend/routes/centerRoutes.js` (신규 생성 필요)

### 🔧 프론트엔드 작업

#### 1.1. API 함수 파일 생성
**파일**: `frontend/src/api/centerApi.js`
```javascript
// 센터 목록 조회
export const fetchCenters = async (query = '', page = 1, pageSize = 7, lat = null, lng = null) => {
  try {
    const params = new URLSearchParams({
      query,
      page: page.toString(),
      pageSize: pageSize.toString(),
    });
    
    if (lat && lng) {
      params.append('lat', lat.toString());
      params.append('lng', lng.toString());
    }
    
    const response = await fetch(`/api/centers?${params}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
      },
    });
    
    if (!response.ok) throw new Error('센터 목록 조회 실패');
    
    return await response.json();
  } catch (error) {
    console.error('센터 목록 조회 에러:', error);
    throw error;
  }
};

// 센터 상세 조회
export const fetchCenterDetail = async (centerId) => {
  try {
    const response = await fetch(`/api/centers/${centerId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
      },
    });
    
    if (!response.ok) throw new Error('센터 상세 조회 실패');
    
    return await response.json();
  } catch (error) {
    console.error('센터 상세 조회 에러:', error);
    throw error;
  }
};
```

#### 1.2. Map.jsx 수정
**파일**: `frontend/src/pages/user/info/Map.jsx`

**삭제할 부분** (더미 데이터):
```javascript
// 58-82번 줄 부근의 centers 더미 데이터 전체 삭제
```

**추가할 부분**:
```javascript
import { fetchCenters, fetchCenterDetail } from '../../../api/centerApi';

// useEffect로 센터 목록 불러오기
useEffect(() => {
  const loadCenters = async () => {
    try {
      setLoading(true);
      
      // 사용자 위치 가져오기
      let userLat = null;
      let userLng = null;
      
      if (navigator.geolocation) {
        const position = await new Promise((resolve, reject) => {
          navigator.geolocation.getCurrentPosition(resolve, reject);
        });
        userLat = position.coords.latitude;
        userLng = position.coords.longitude;
      }
      
      // API 호출
      const data = await fetchCenters(searchQuery, currentPage, 7, userLat, userLng);
      
      setCenters(data.centers);
      setTotalPages(data.totalPages);
      
      // 카카오맵 마커 업데이트
      if (mapRef.current && data.centers.length > 0) {
        updateMapMarkers(data.centers);
      }
    } catch (error) {
      console.error('센터 목록 로드 실패:', error);
      alert('센터 정보를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };
  
  loadCenters();
}, [searchQuery, currentPage]);
```

### 🗄️ DB 스키마
```sql
CREATE TABLE centers (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(200) NOT NULL,
  address VARCHAR(500) NOT NULL,
  phone VARCHAR(20),
  latitude DECIMAL(10, 8) NOT NULL,
  longitude DECIMAL(11, 8) NOT NULL,
  business_hours VARCHAR(200),
  description TEXT,
  website VARCHAR(500),
  category VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_location (latitude, longitude),
  INDEX idx_name (name),
  INDEX idx_category (category)
);
```

### 🔌 백엔드 API

#### 1.3. Express 라우트 생성
**파일**: `backend/routes/centerRoutes.js`
```javascript
const express = require('express');
const router = express.Router();
const { authenticateToken } = require('../middleware/auth');
const db = require('../config/database');

// 센터 목록 조회 (검색, 페이지네이션, 거리 계산)
router.get('/centers', authenticateToken, async (req, res) => {
  try {
    const { query = '', page = 1, pageSize = 7, lat, lng } = req.query;
    const offset = (page - 1) * pageSize;
    
    let sql = `
      SELECT 
        id, name, address, phone, latitude, longitude,
        business_hours, description, website, category
    `;
    
    // 사용자 위치가 있으면 거리 계산
    if (lat && lng) {
      sql += `,
        (6371 * acos(
          cos(radians(?)) * cos(radians(latitude)) *
          cos(radians(longitude) - radians(?)) +
          sin(radians(?)) * sin(radians(latitude))
        )) AS distance_km
      `;
    }
    
    sql += ` FROM centers WHERE 1=1`;
    
    const params = [];
    if (lat && lng) {
      params.push(lat, lng, lat);
    }
    
    // 검색 조건
    if (query) {
      sql += ` AND (name LIKE ? OR address LIKE ?)`;
      params.push(`%${query}%`, `%${query}%`);
    }
    
    // 거리순 정렬 (위치 있으면) 또는 이름순
    sql += lat && lng ? ` ORDER BY distance_km ASC` : ` ORDER BY name ASC`;
    
    sql += ` LIMIT ? OFFSET ?`;
    params.push(parseInt(pageSize), parseInt(offset));
    
    const [centers] = await db.query(sql, params);
    
    // 전체 개수 조회
    let countSql = `SELECT COUNT(*) as total FROM centers WHERE 1=1`;
    const countParams = [];
    if (query) {
      countSql += ` AND (name LIKE ? OR address LIKE ?)`;
      countParams.push(`%${query}%`, `%${query}%`);
    }
    
    const [countResult] = await db.query(countSql, countParams);
    const totalCount = countResult[0].total;
    const totalPages = Math.ceil(totalCount / pageSize);
    
    res.json({
      centers,
      totalCount,
      totalPages,
      currentPage: parseInt(page),
      currentLocation: lat && lng ? { lat: parseFloat(lat), lng: parseFloat(lng) } : null,
    });
  } catch (error) {
    console.error('센터 목록 조회 오류:', error);
    res.status(500).json({ error: '센터 목록 조회 실패' });
  }
});

// 센터 상세 조회
router.get('/centers/:id', authenticateToken, async (req, res) => {
  try {
    const { id } = req.params;
    
    const [centers] = await db.query(
      'SELECT * FROM centers WHERE id = ?',
      [id]
    );
    
    if (centers.length === 0) {
      return res.status(404).json({ error: '센터를 찾을 수 없습니다' });
    }
    
    res.json(centers[0]);
  } catch (error) {
    console.error('센터 상세 조회 오류:', error);
    res.status(500).json({ error: '센터 상세 조회 실패' });
  }
});

module.exports = router;
```

#### 1.4. Kakao Maps API 설정
**파일**: `frontend/public/index.html`
```html
<!-- Kakao Maps API 추가 -->
<script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=YOUR_KAKAO_APP_KEY"></script>
```

**환경 변수**: `.env`
```
VITE_KAKAO_MAP_API_KEY=your_kakao_map_api_key_here
```

---

## 2. 리뷰 CRUD

### 📁 파일 위치
- **프론트엔드**: 
  - `frontend/src/pages/system/info/ReviewList.jsx`
  - `frontend/src/pages/system/info/ReviewDetail.jsx`
- **API 파일 생성**: `frontend/src/api/reviewApi.js` (신규 생성 필요)
- **백엔드**: `backend/routes/reviewRoutes.js` (신규 생성 필요)

### 🔧 프론트엔드 작업

#### 2.1. API 함수 파일 생성
**파일**: `frontend/src/api/reviewApi.js`
```javascript
// 리뷰 목록 조회
export const fetchReviews = async (counselorId, page = 1, pageSize = 10) => {
  try {
    const response = await fetch(
      `/api/counselors/${counselorId}/reviews?page=${page}&pageSize=${pageSize}`,
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      }
    );
    
    if (!response.ok) throw new Error('리뷰 목록 조회 실패');
    
    return await response.json();
  } catch (error) {
    console.error('리뷰 목록 조회 에러:', error);
    throw error;
  }
};

// 리뷰 상세 조회
export const fetchReviewDetail = async (reviewId) => {
  try {
    const response = await fetch(`/api/reviews/${reviewId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
      },
    });
    
    if (!response.ok) throw new Error('리뷰 상세 조회 실패');
    
    return await response.json();
  } catch (error) {
    console.error('리뷰 상세 조회 에러:', error);
    throw error;
  }
};

// 리뷰 답글 작성
export const createReviewReply = async (reviewId, replyContent) => {
  try {
    const response = await fetch(`/api/reviews/${reviewId}/reply`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
      },
      body: JSON.stringify({ content: replyContent }),
    });
    
    if (!response.ok) throw new Error('답글 작성 실패');
    
    return await response.json();
  } catch (error) {
    console.error('답글 작성 에러:', error);
    throw error;
  }
};

// 리뷰 답글 수정
export const updateReviewReply = async (reviewId, replyContent) => {
  try {
    const response = await fetch(`/api/reviews/${reviewId}/reply`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
      },
      body: JSON.stringify({ content: replyContent }),
    });
    
    if (!response.ok) throw new Error('답글 수정 실패');
    
    return await response.json();
  } catch (error) {
    console.error('답글 수정 에러:', error);
    throw error;
  }
};

// 리뷰 답글 삭제
export const deleteReviewReply = async (reviewId) => {
  try {
    const response = await fetch(`/api/reviews/${reviewId}/reply`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
      },
    });
    
    if (!response.ok) throw new Error('답글 삭제 실패');
    
    return await response.json();
  } catch (error) {
    console.error('답글 삭제 에러:', error);
    throw error;
  }
};
```

#### 2.2. ReviewList.jsx 수정
**파일**: `frontend/src/pages/system/info/ReviewList.jsx`

**삭제할 부분**:
```javascript
// 3번 줄의 import { counselorReviews } from './counselorProfileData'; 삭제
// 더미 데이터 사용 부분 삭제
```

**추가할 부분**:
```javascript
import { fetchReviews } from '../../../api/reviewApi';
import { useAuth } from '../../../hooks/useAuth';

const ReviewList = () => {
  const { user } = useAuth();
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(false);
  const [totalPages, setTotalPages] = useState(1);
  
  useEffect(() => {
    const loadReviews = async () => {
      try {
        setLoading(true);
        const data = await fetchReviews(user.id, currentPage, itemsPerPage);
        
        setReviews(data.reviews);
        setTotalPages(data.totalPages);
      } catch (error) {
        console.error('리뷰 로드 실패:', error);
        alert('리뷰를 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };
    
    loadReviews();
  }, [currentPage, user.id]);
  
  // ... 나머지 코드
};
```

#### 2.3. ReviewDetail.jsx 수정
**파일**: `frontend/src/pages/system/info/ReviewDetail.jsx`

**추가/수정**:
```javascript
import { fetchReviewDetail, createReviewReply, updateReviewReply, deleteReviewReply } from '../../../api/reviewApi';

const ReviewDetail = () => {
  const { reviewId } = useParams();
  const [review, setReview] = useState(null);
  const [loading, setLoading] = useState(false);
  const [replyText, setReplyText] = useState('');
  const [isEditing, setIsEditing] = useState(false);
  
  useEffect(() => {
    const loadReview = async () => {
      try {
        setLoading(true);
        const data = await fetchReviewDetail(reviewId);
        
        setReview(data);
        if (data.reply) {
          setReplyText(data.reply.content);
        }
      } catch (error) {
        console.error('리뷰 로드 실패:', error);
        alert('리뷰를 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };
    
    loadReview();
  }, [reviewId]);
  
  const handleSubmitReply = async () => {
    if (!replyText.trim()) {
      alert('답글 내용을 입력해주세요.');
      return;
    }
    
    try {
      if (review.reply) {
        // 수정
        await updateReviewReply(reviewId, replyText);
        alert('답글이 수정되었습니다.');
      } else {
        // 등록
        await createReviewReply(reviewId, replyText);
        alert('답글이 등록되었습니다.');
      }
      
      // 리뷰 다시 불러오기
      const data = await fetchReviewDetail(reviewId);
      setReview(data);
      setIsEditing(false);
    } catch (error) {
      console.error('답글 처리 실패:', error);
      alert('답글 처리에 실패했습니다.');
    }
  };
  
  const handleDeleteReply = async () => {
    if (!window.confirm('답글을 삭제하시겠습니까?')) return;
    
    try {
      await deleteReviewReply(reviewId);
      alert('답글이 삭제되었습니다.');
      
      // 리뷰 다시 불러오기
      const data = await fetchReviewDetail(reviewId);
      setReview(data);
      setReplyText('');
    } catch (error) {
      console.error('답글 삭제 실패:', error);
      alert('답글 삭제에 실패했습니다.');
    }
  };
  
  // ... 나머지 코드
};
```

### 🗄️ DB 스키마
```sql
CREATE TABLE reviews (
  id INT PRIMARY KEY AUTO_INCREMENT,
  counselor_id INT NOT NULL,
  user_id INT NOT NULL,
  counsel_id INT NOT NULL,
  rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (counsel_id) REFERENCES counsels(id) ON DELETE CASCADE,
  INDEX idx_counselor (counselor_id),
  INDEX idx_user (user_id),
  INDEX idx_rating (rating),
  INDEX idx_created (created_at DESC)
);

CREATE TABLE review_replies (
  id INT PRIMARY KEY AUTO_INCREMENT,
  review_id INT NOT NULL,
  counselor_id INT NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE,
  FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_review (review_id)
);
```

### 🔌 백엔드 API

**파일**: `backend/routes/reviewRoutes.js`
```javascript
const express = require('express');
const router = express.Router();
const { authenticateToken, requireCounselor } = require('../middleware/auth');
const db = require('../config/database');

// 상담사의 리뷰 목록 조회
router.get('/counselors/:counselorId/reviews', authenticateToken, async (req, res) => {
  try {
    const { counselorId } = req.params;
    const { page = 1, pageSize = 10 } = req.query;
    const offset = (page - 1) * pageSize;
    
    const [reviews] = await db.query(
      `SELECT 
        r.*,
        u.name as user_name,
        rr.content as reply_content,
        rr.created_at as reply_created_at
      FROM reviews r
      JOIN users u ON r.user_id = u.id
      LEFT JOIN review_replies rr ON r.id = rr.review_id
      WHERE r.counselor_id = ?
      ORDER BY r.created_at DESC
      LIMIT ? OFFSET ?`,
      [counselorId, parseInt(pageSize), parseInt(offset)]
    );
    
    const [countResult] = await db.query(
      'SELECT COUNT(*) as total FROM reviews WHERE counselor_id = ?',
      [counselorId]
    );
    
    const totalCount = countResult[0].total;
    const totalPages = Math.ceil(totalCount / pageSize);
    
    res.json({
      reviews,
      totalCount,
      totalPages,
      currentPage: parseInt(page),
    });
  } catch (error) {
    console.error('리뷰 목록 조회 오류:', error);
    res.status(500).json({ error: '리뷰 목록 조회 실패' });
  }
});

// 리뷰 상세 조회
router.get('/reviews/:id', authenticateToken, async (req, res) => {
  try {
    const { id } = req.params;
    
    const [reviews] = await db.query(
      `SELECT 
        r.*,
        u.name as user_name,
        u.mbti as user_mbti
      FROM reviews r
      JOIN users u ON r.user_id = u.id
      WHERE r.id = ?`,
      [id]
    );
    
    if (reviews.length === 0) {
      return res.status(404).json({ error: '리뷰를 찾을 수 없습니다' });
    }
    
    const review = reviews[0];
    
    // 답글 조회
    const [replies] = await db.query(
      'SELECT * FROM review_replies WHERE review_id = ?',
      [id]
    );
    
    review.reply = replies.length > 0 ? replies[0] : null;
    
    res.json(review);
  } catch (error) {
    console.error('리뷰 상세 조회 오류:', error);
    res.status(500).json({ error: '리뷰 상세 조회 실패' });
  }
});

// 리뷰 답글 작성
router.post('/reviews/:id/reply', authenticateToken, requireCounselor, async (req, res) => {
  try {
    const { id } = req.params;
    const { content } = req.body;
    const counselorId = req.user.id;
    
    // 리뷰 존재 여부 및 권한 확인
    const [reviews] = await db.query(
      'SELECT counselor_id FROM reviews WHERE id = ?',
      [id]
    );
    
    if (reviews.length === 0) {
      return res.status(404).json({ error: '리뷰를 찾을 수 없습니다' });
    }
    
    if (reviews[0].counselor_id !== counselorId) {
      return res.status(403).json({ error: '답글 작성 권한이 없습니다' });
    }
    
    // 이미 답글이 있는지 확인
    const [existingReplies] = await db.query(
      'SELECT id FROM review_replies WHERE review_id = ?',
      [id]
    );
    
    if (existingReplies.length > 0) {
      return res.status(400).json({ error: '이미 답글이 존재합니다' });
    }
    
    // 답글 저장
    const [result] = await db.query(
      'INSERT INTO review_replies (review_id, counselor_id, content) VALUES (?, ?, ?)',
      [id, counselorId, content]
    );
    
    res.json({
      success: true,
      replyId: result.insertId,
      message: '답글이 등록되었습니다',
    });
  } catch (error) {
    console.error('답글 작성 오류:', error);
    res.status(500).json({ error: '답글 작성 실패' });
  }
});

// 리뷰 답글 수정
router.put('/reviews/:id/reply', authenticateToken, requireCounselor, async (req, res) => {
  try {
    const { id } = req.params;
    const { content } = req.body;
    const counselorId = req.user.id;
    
    // 권한 확인
    const [replies] = await db.query(
      `SELECT rr.id 
       FROM review_replies rr
       JOIN reviews r ON rr.review_id = r.id
       WHERE rr.review_id = ? AND r.counselor_id = ?`,
      [id, counselorId]
    );
    
    if (replies.length === 0) {
      return res.status(404).json({ error: '답글을 찾을 수 없거나 권한이 없습니다' });
    }
    
    // 답글 수정
    await db.query(
      'UPDATE review_replies SET content = ?, updated_at = NOW() WHERE review_id = ?',
      [content, id]
    );
    
    res.json({
      success: true,
      message: '답글이 수정되었습니다',
    });
  } catch (error) {
    console.error('답글 수정 오류:', error);
    res.status(500).json({ error: '답글 수정 실패' });
  }
});

// 리뷰 답글 삭제
router.delete('/reviews/:id/reply', authenticateToken, requireCounselor, async (req, res) => {
  try {
    const { id } = req.params;
    const counselorId = req.user.id;
    
    // 권한 확인
    const [replies] = await db.query(
      `SELECT rr.id 
       FROM review_replies rr
       JOIN reviews r ON rr.review_id = r.id
       WHERE rr.review_id = ? AND r.counselor_id = ?`,
      [id, counselorId]
    );
    
    if (replies.length === 0) {
      return res.status(404).json({ error: '답글을 찾을 수 없거나 권한이 없습니다' });
    }
    
    // 답글 삭제
    await db.query('DELETE FROM review_replies WHERE review_id = ?', [id]);
    
    res.json({
      success: true,
      message: '답글이 삭제되었습니다',
    });
  } catch (error) {
    console.error('답글 삭제 오류:', error);
    res.status(500).json({ error: '답글 삭제 실패' });
  }
});

module.exports = router;
```

---

## 3. 최근 민감 키워드 리스트 (관리자)

### 📁 파일 위치
- **프론트엔드**: 
  - `frontend/src/pages/admin/Alarm.jsx`
  - `frontend/src/pages/admin/Statistics.jsx`
- **API 파일 생성**: `frontend/src/api/adminApi.js` (신규 생성 필요)
- **백엔드**: `backend/routes/adminRoutes.js` (신규 생성 필요)

### 🔧 프론트엔드 작업

#### 3.1. API 함수 파일 생성
**파일**: `frontend/src/api/adminApi.js`
```javascript
// 위험 단어 감지 알림 조회
export const fetchRiskAlerts = async (page = 1, pageSize = 10, status = 'all') => {
  try {
    const response = await fetch(
      `/api/admin/risk-alerts?page=${page}&pageSize=${pageSize}&status=${status}`,
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      }
    );
    
    if (!response.ok) throw new Error('위험 알림 조회 실패');
    
    return await response.json();
  } catch (error) {
    console.error('위험 알림 조회 에러:', error);
    throw error;
  }
};

// 위험 알림 처리
export const resolveRiskAlert = async (alertId, action, note) => {
  try {
    const response = await fetch(`/api/admin/risk-alerts/${alertId}/resolve`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
      },
      body: JSON.stringify({ action, note }),
    });
    
    if (!response.ok) throw new Error('알림 처리 실패');
    
    return await response.json();
  } catch (error) {
    console.error('알림 처리 에러:', error);
    throw error;
  }
};

// 키워드 통계 조회
export const fetchKeywordStats = async (startDate, endDate) => {
  try {
    const response = await fetch(
      `/api/admin/statistics/keywords?startDate=${startDate}&endDate=${endDate}`,
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      }
    );
    
    if (!response.ok) throw new Error('키워드 통계 조회 실패');
    
    return await response.json();
  } catch (error) {
    console.error('키워드 통계 조회 에러:', error);
    throw error;
  }
};

// 공지사항 조회
export const fetchNotices = async (page = 1, pageSize = 10) => {
  try {
    const response = await fetch(
      `/api/admin/notices?page=${page}&pageSize=${pageSize}`,
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      }
    );
    
    if (!response.ok) throw new Error('공지사항 조회 실패');
    
    return await response.json();
  } catch (error) {
    console.error('공지사항 조회 에러:', error);
    throw error;
  }
};
```

#### 3.2. Alarm.jsx 수정
**파일**: `frontend/src/pages/admin/Alarm.jsx`

**삭제할 부분**:
```javascript
// 더미 데이터 부분 (52-88번 줄 부근) 전체 삭제
```

**추가할 부분**:
```javascript
import { fetchRiskAlerts, fetchNotices, resolveRiskAlert } from '../../api/adminApi';

const Alarm = () => {
  const [alerts, setAlerts] = useState([]);
  const [notices, setNotices] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filterStatus, setFilterStatus] = useState('pending');
  
  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        
        // 위험 알림 조회
        const alertsData = await fetchRiskAlerts(1, 10, filterStatus);
        setAlerts(alertsData.alerts);
        
        // 공지사항 조회
        const noticesData = await fetchNotices(1, 5);
        setNotices(noticesData.notices);
      } catch (error) {
        console.error('데이터 로드 실패:', error);
        alert('데이터를 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };
    
    loadData();
  }, [filterStatus]);
  
  const handleResolveAlert = async (alertId) => {
    if (!window.confirm('이 알림을 처리하시겠습니까?')) return;
    
    try {
      await resolveRiskAlert(alertId, 'resolved', '관리자가 확인 완료');
      alert('알림이 처리되었습니다.');
      
      // 목록 새로고침
      const alertsData = await fetchRiskAlerts(1, 10, filterStatus);
      setAlerts(alertsData.alerts);
    } catch (error) {
      console.error('알림 처리 실패:', error);
      alert('알림 처리에 실패했습니다.');
    }
  };
  
  // ... 나머지 코드
};
```

#### 3.3. Statistics.jsx 수정
**파일**: `frontend/src/pages/admin/Statistics.jsx`

**추가/수정**:
```javascript
import { fetchKeywordStats } from '../../api/adminApi';

const Statistics = () => {
  const [chartData, setChartData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [dateRange, setDateRange] = useState({
    start: '2026-01-19',
    end: '2026-01-25'
  });
  
  useEffect(() => {
    const loadStats = async () => {
      try {
        setLoading(true);
        
        const data = await fetchKeywordStats(dateRange.start, dateRange.end);
        setChartData(data.keywords);
      } catch (error) {
        console.error('통계 로드 실패:', error);
        alert('통계를 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };
    
    loadStats();
  }, [dateRange]);
  
  // ... 나머지 코드
};
```

### 🗄️ DB 스키마
```sql
CREATE TABLE risk_alerts (
  id INT PRIMARY KEY AUTO_INCREMENT,
  type ENUM('concern', 'career', 'job') NOT NULL,
  counselor_id INT NOT NULL,
  user_id INT NOT NULL,
  counsel_id INT,
  keyword VARCHAR(100) NOT NULL,
  risk_level ENUM('high', 'medium', 'low') DEFAULT 'medium',
  status ENUM('pending', 'resolved') DEFAULT 'pending',
  content TEXT,
  detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  resolved_at TIMESTAMP NULL,
  resolved_by INT NULL,
  resolution_note TEXT NULL,
  FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (counsel_id) REFERENCES counsels(id) ON DELETE SET NULL,
  FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL,
  INDEX idx_status (status),
  INDEX idx_detected (detected_at DESC),
  INDEX idx_keyword (keyword)
);

CREATE TABLE keyword_stats (
  id INT PRIMARY KEY AUTO_INCREMENT,
  keyword VARCHAR(100) NOT NULL,
  count INT DEFAULT 1,
  date DATE NOT NULL,
  category VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY unique_keyword_date (keyword, date),
  INDEX idx_date (date),
  INDEX idx_count (count DESC)
);

CREATE TABLE notices (
  id INT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  created_by INT NOT NULL,
  is_important BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_created (created_at DESC)
);

CREATE TABLE notice_reads (
  id INT PRIMARY KEY AUTO_INCREMENT,
  notice_id INT NOT NULL,
  user_id INT NOT NULL,
  read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (notice_id) REFERENCES notices(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY unique_read (notice_id, user_id),
  INDEX idx_user (user_id)
);
```

### 🔌 백엔드 API

**파일**: `backend/routes/adminRoutes.js`
```javascript
const express = require('express');
const router = express.Router();
const { authenticateToken, requireAdmin } = require('../middleware/auth');
const db = require('../config/database');

// 위험 알림 조회
router.get('/admin/risk-alerts', authenticateToken, requireAdmin, async (req, res) => {
  try {
    const { page = 1, pageSize = 10, status = 'all' } = req.query;
    const offset = (page - 1) * pageSize;
    
    let sql = `
      SELECT 
        ra.*,
        u1.name as counselor_name,
        u2.name as user_name
      FROM risk_alerts ra
      JOIN users u1 ON ra.counselor_id = u1.id
      JOIN users u2 ON ra.user_id = u2.id
      WHERE 1=1
    `;
    
    const params = [];
    
    if (status !== 'all') {
      sql += ` AND ra.status = ?`;
      params.push(status);
    }
    
    sql += ` ORDER BY ra.detected_at DESC LIMIT ? OFFSET ?`;
    params.push(parseInt(pageSize), parseInt(offset));
    
    const [alerts] = await db.query(sql, params);
    
    // 전체 개수
    let countSql = 'SELECT COUNT(*) as total FROM risk_alerts WHERE 1=1';
    const countParams = [];
    
    if (status !== 'all') {
      countSql += ` AND status = ?`;
      countParams.push(status);
    }
    
    const [countResult] = await db.query(countSql, countParams);
    const totalCount = countResult[0].total;
    
    res.json({
      alerts,
      totalCount,
      currentPage: parseInt(page),
    });
  } catch (error) {
    console.error('위험 알림 조회 오류:', error);
    res.status(500).json({ error: '위험 알림 조회 실패' });
  }
});

// 위험 알림 처리
router.put('/admin/risk-alerts/:id/resolve', authenticateToken, requireAdmin, async (req, res) => {
  try {
    const { id } = req.params;
    const { action, note } = req.body;
    const adminId = req.user.id;
    
    await db.query(
      `UPDATE risk_alerts 
       SET status = 'resolved', 
           resolved_at = NOW(), 
           resolved_by = ?, 
           resolution_note = ?
       WHERE id = ?`,
      [adminId, note, id]
    );
    
    res.json({
      success: true,
      message: '알림이 처리되었습니다',
    });
  } catch (error) {
    console.error('알림 처리 오류:', error);
    res.status(500).json({ error: '알림 처리 실패' });
  }
});

// 키워드 통계 조회
router.get('/admin/statistics/keywords', authenticateToken, requireAdmin, async (req, res) => {
  try {
    const { startDate, endDate } = req.query;
    
    const [keywords] = await db.query(
      `SELECT 
        keyword as label,
        SUM(count) as count
      FROM keyword_stats
      WHERE date BETWEEN ? AND ?
      GROUP BY keyword
      ORDER BY count DESC
      LIMIT 10`,
      [startDate, endDate]
    );
    
    // 전체 개수
    const totalCount = keywords.reduce((sum, k) => sum + k.count, 0);
    
    // 퍼센티지 계산
    const keywordsWithPercentage = keywords.map(k => ({
      ...k,
      percentage: ((k.count / totalCount) * 100).toFixed(1),
      color: `#${Math.floor(Math.random()*16777215).toString(16)}`, // 랜덤 색상
    }));
    
    res.json({
      keywords: keywordsWithPercentage,
      totalCount,
      period: { start: startDate, end: endDate },
    });
  } catch (error) {
    console.error('키워드 통계 조회 오류:', error);
    res.status(500).json({ error: '키워드 통계 조회 실패' });
  }
});

// 공지사항 조회
router.get('/admin/notices', authenticateToken, async (req, res) => {
  try {
    const { page = 1, pageSize = 10 } = req.query;
    const offset = (page - 1) * pageSize;
    const userId = req.user.id;
    
    const [notices] = await db.query(
      `SELECT 
        n.*,
        u.name as author_name,
        CASE WHEN nr.id IS NOT NULL THEN TRUE ELSE FALSE END as is_read
      FROM notices n
      JOIN users u ON n.created_by = u.id
      LEFT JOIN notice_reads nr ON n.id = nr.notice_id AND nr.user_id = ?
      ORDER BY n.is_important DESC, n.created_at DESC
      LIMIT ? OFFSET ?`,
      [userId, parseInt(pageSize), parseInt(offset)]
    );
    
    const [countResult] = await db.query('SELECT COUNT(*) as total FROM notices');
    const totalCount = countResult[0].total;
    const totalPages = Math.ceil(totalCount / pageSize);
    
    res.json({
      notices,
      totalCount,
      totalPages,
      currentPage: parseInt(page),
    });
  } catch (error) {
    console.error('공지사항 조회 오류:', error);
    res.status(500).json({ error: '공지사항 조회 실패' });
  }
});

module.exports = router;
```

---

## 4. 최근 활동 내역 (상담사)

### 📁 파일 위치
- **프론트엔드**: `frontend/src/pages/system/info/MyCounsel.jsx`
- **API 파일**: `frontend/src/api/counselApi.js` (이미 존재)
- **백엔드**: `backend/routes/counselRoutes.js` (신규 생성 필요)

### 🔧 프론트엔드 작업

#### 4.1. MyCounsel.jsx 확인
**파일**: `frontend/src/pages/system/info/MyCounsel.jsx`

이미 `counselApi.js`를 사용하고 있으며, TODO 주석에 DB 연동 가이드가 작성되어 있습니다.

**수정 필요 부분**:
```javascript
// 더미 데이터 부분 삭제
// ========== 더미 데이터 시작 (DB 연동 시 아래 전체 삭제) ==========
// ... 더미 데이터 ...
// ========== 더미 데이터 끝 (여기까지 삭제) ==========

// 이 부분을 실제 API 호출로 교체
useEffect(() => {
  const loadCounselData = async () => {
    try {
      setLoading(true);
      
      const [stats, timeline, counsels] = await Promise.all([
        fetchCounselStats(),
        fetchCounselTimeline(),
        fetchAllCounsels(),
      ]);
      
      // 데이터 설정
      // ...
    } catch (error) {
      console.error('상담 데이터 로드 실패:', error);
    } finally {
      setLoading(false);
    }
  };
  
  loadCounselData();
}, []);
```

### 🗄️ DB 스키마
```sql
CREATE TABLE counsels (
  id INT PRIMARY KEY AUTO_INCREMENT,
  counselor_id INT NOT NULL,
  user_id INT NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  detailed_content TEXT,
  status ENUM('scheduled', 'inProgress', 'completed', 'cancelled') DEFAULT 'scheduled',
  counsel_type ENUM('chat', 'video', 'phone') DEFAULT 'chat',
  reservation_date DATETIME NOT NULL,
  started_at DATETIME NULL,
  completed_at DATETIME NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_counselor_status (counselor_id, status),
  INDEX idx_user (user_id),
  INDEX idx_reservation (reservation_date),
  INDEX idx_status (status)
);

CREATE TABLE counsel_messages (
  id INT PRIMARY KEY AUTO_INCREMENT,
  counsel_id INT NOT NULL,
  sender_id INT NOT NULL,
  sender_type ENUM('counselor', 'client') NOT NULL,
  message TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (counsel_id) REFERENCES counsels(id) ON DELETE CASCADE,
  FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_counsel (counsel_id),
  INDEX idx_created (created_at)
);

CREATE TABLE counsel_timeline (
  id INT PRIMARY KEY AUTO_INCREMENT,
  counselor_id INT NOT NULL,
  action_type ENUM('request', 'accept', 'start', 'complete', 'cancel') NOT NULL,
  description VARCHAR(500) NOT NULL,
  counsel_id INT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (counsel_id) REFERENCES counsels(id) ON DELETE SET NULL,
  INDEX idx_counselor_created (counselor_id, created_at DESC)
);
```

### 🔌 백엔드 API

**파일**: `backend/routes/counselRoutes.js`
```javascript
const express = require('express');
const router = express.Router();
const { authenticateToken, requireCounselor } = require('../middleware/auth');
const db = require('../config/database');

// 상담 통계 조회
router.get('/counselors/me/stats', authenticateToken, requireCounselor, async (req, res) => {
  try {
    const counselorId = req.user.id;
    const today = new Date();
    const sevenDaysAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
    
    // 7일간 완료된 상담
    const [completedResult] = await db.query(
      `SELECT COUNT(*) as count 
       FROM counsels 
       WHERE counselor_id = ? 
         AND status = 'completed' 
         AND completed_at >= ?`,
      [counselorId, sevenDaysAgo]
    );
    
    // 예약된 상담
    const [reservedResult] = await db.query(
      `SELECT COUNT(*) as count 
       FROM counsels 
       WHERE counselor_id = ? 
         AND status = 'scheduled'`,
      [counselorId]
    );
    
    // 전체 상담 (7일간)
    const [totalResult] = await db.query(
      `SELECT COUNT(*) as count 
       FROM counsels 
       WHERE counselor_id = ? 
         AND created_at >= ?`,
      [counselorId, sevenDaysAgo]
    );
    
    // 위험 단어 감지
    const [riskResult] = await db.query(
      `SELECT COUNT(*) as count 
       FROM risk_alerts 
       WHERE counselor_id = ? 
         AND detected_at >= ? 
         AND status = 'pending'`,
      [counselorId, sevenDaysAgo]
    );
    
    res.json({
      riskCount: riskResult[0].count,
      completedCount: completedResult[0].count,
      reservedCount: reservedResult[0].count,
      totalCount: totalResult[0].count,
    });
  } catch (error) {
    console.error('상담 통계 조회 오류:', error);
    res.status(500).json({ error: '통계 조회 실패' });
  }
});

// 활동 타임라인 조회
router.get('/counselors/me/timeline', authenticateToken, requireCounselor, async (req, res) => {
  try {
    const counselorId = req.user.id;
    
    const [timeline] = await db.query(
      `SELECT * FROM counsel_timeline 
       WHERE counselor_id = ? 
       ORDER BY created_at DESC 
       LIMIT 10`,
      [counselorId]
    );
    
    res.json({ timeline });
  } catch (error) {
    console.error('타임라인 조회 오류:', error);
    res.status(500).json({ error: '타임라인 조회 실패' });
  }
});

// 상담 목록 조회 (전체)
router.get('/counselors/me/counsels', authenticateToken, requireCounselor, async (req, res) => {
  try {
    const counselorId = req.user.id;
    const { status } = req.query;
    
    let sql = `
      SELECT 
        c.*,
        u.name as client_name
      FROM counsels c
      JOIN users u ON c.user_id = u.id
      WHERE c.counselor_id = ?
    `;
    
    const params = [counselorId];
    
    if (status) {
      sql += ` AND c.status = ?`;
      params.push(status);
    }
    
    sql += ` ORDER BY c.reservation_date DESC`;
    
    const [counsels] = await db.query(sql, params);
    
    res.json({ counsels });
  } catch (error) {
    console.error('상담 목록 조회 오류:', error);
    res.status(500).json({ error: '상담 목록 조회 실패' });
  }
});

// 상담 상세 조회
router.get('/counselors/me/counsels/:id', authenticateToken, requireCounselor, async (req, res) => {
  try {
    const { id } = req.params;
    const counselorId = req.user.id;
    
    const [counsels] = await db.query(
      `SELECT 
        c.*,
        u.name as client_name,
        u.mbti as client_mbti,
        u.gender as client_gender,
        u.age as client_age
      FROM counsels c
      JOIN users u ON c.user_id = u.id
      WHERE c.id = ? AND c.counselor_id = ?`,
      [id, counselorId]
    );
    
    if (counsels.length === 0) {
      return res.status(404).json({ error: '상담을 찾을 수 없습니다' });
    }
    
    const counsel = counsels[0];
    
    // 메시지 조회 (진행중/완료 상태인 경우)
    if (counsel.status === 'inProgress' || counsel.status === 'completed') {
      const [messages] = await db.query(
        `SELECT * FROM counsel_messages 
         WHERE counsel_id = ? 
         ORDER BY created_at ASC`,
        [id]
      );
      
      counsel.messages = messages;
    }
    
    res.json(counsel);
  } catch (error) {
    console.error('상담 상세 조회 오류:', error);
    res.status(500).json({ error: '상담 상세 조회 실패' });
  }
});

// 상담 시작
router.post('/counselors/me/counsels/:id/start', authenticateToken, requireCounselor, async (req, res) => {
  try {
    const { id } = req.params;
    const counselorId = req.user.id;
    
    // 상담 상태 확인
    const [counsels] = await db.query(
      'SELECT status FROM counsels WHERE id = ? AND counselor_id = ?',
      [id, counselorId]
    );
    
    if (counsels.length === 0) {
      return res.status(404).json({ error: '상담을 찾을 수 없습니다' });
    }
    
    if (counsels[0].status !== 'scheduled') {
      return res.status(400).json({ error: '이미 시작되었거나 완료된 상담입니다' });
    }
    
    // 상담 시작
    await db.query(
      `UPDATE counsels 
       SET status = 'inProgress', started_at = NOW() 
       WHERE id = ?`,
      [id]
    );
    
    // 타임라인 추가
    await db.query(
      `INSERT INTO counsel_timeline (counselor_id, action_type, description, counsel_id)
       VALUES (?, 'start', '상담을 시작했습니다', ?)`,
      [counselorId, id]
    );
    
    res.json({
      success: true,
      message: '상담이 시작되었습니다',
    });
  } catch (error) {
    console.error('상담 시작 오류:', error);
    res.status(500).json({ error: '상담 시작 실패' });
  }
});

// 상담 완료
router.post('/counselors/me/counsels/:id/complete', authenticateToken, requireCounselor, async (req, res) => {
  try {
    const { id } = req.params;
    const counselorId = req.user.id;
    
    // 상담 완료
    await db.query(
      `UPDATE counsels 
       SET status = 'completed', completed_at = NOW() 
       WHERE id = ? AND counselor_id = ?`,
      [id, counselorId]
    );
    
    // 타임라인 추가
    await db.query(
      `INSERT INTO counsel_timeline (counselor_id, action_type, description, counsel_id)
       VALUES (?, 'complete', '상담을 완료했습니다', ?)`,
      [counselorId, id]
    );
    
    res.json({
      success: true,
      message: '상담이 완료되었습니다',
    });
  } catch (error) {
    console.error('상담 완료 오류:', error);
    res.status(500).json({ error: '상담 완료 실패' });
  }
});

// 메시지 전송
router.post('/counselors/me/counsels/:id/messages', authenticateToken, requireCounselor, async (req, res) => {
  try {
    const { id } = req.params;
    const { message } = req.body;
    const counselorId = req.user.id;
    
    // 메시지 저장
    const [result] = await db.query(
      `INSERT INTO counsel_messages (counsel_id, sender_id, sender_type, message)
       VALUES (?, ?, 'counselor', ?)`,
      [id, counselorId, message]
    );
    
    res.json({
      success: true,
      messageId: result.insertId,
    });
  } catch (error) {
    console.error('메시지 전송 오류:', error);
    res.status(500).json({ error: '메시지 전송 실패' });
  }
});

module.exports = router;
```

---

## 5. 백엔드 구조 설정

### 📁 디렉토리 구조 생성
```
backend/
├── config/
│   ├── database.js          # DB 연결 설정
│   └── env.js               # 환경 변수 설정
├── middleware/
│   ├── auth.js              # 인증 미들웨어
│   └── errorHandler.js      # 에러 핸들러
├── routes/
│   ├── authRoutes.js        # 인증 관련
│   ├── centerRoutes.js      # 센터 관련
│   ├── reviewRoutes.js      # 리뷰 관련
│   ├── adminRoutes.js       # 관리자 관련
│   └── counselRoutes.js     # 상담 관련
├── utils/
│   ├── logger.js            # 로깅
│   └── validator.js         # 유효성 검증
├── app.js                   # Express 앱 설정
├── server.js                # 서버 시작
├── package.json
└── .env
```

### 🔧 핵심 파일 생성

#### 5.1. DB 연결 설정
**파일**: `backend/config/database.js`
```javascript
const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: process.env.DB_HOST || 'localhost',
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  database: process.env.DB_NAME || 'gominsoons ak',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0,
});

module.exports = pool;
```

#### 5.2. 인증 미들웨어
**파일**: `backend/middleware/auth.js`
```javascript
const jwt = require('jsonwebtoken');

// JWT 토큰 검증
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];
  
  if (!token) {
    return res.status(401).json({ error: '인증 토큰이 필요합니다' });
  }
  
  jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(403).json({ error: '유효하지 않은 토큰입니다' });
    }
    
    req.user = user;
    next();
  });
};

// 상담사 권한 확인
const requireCounselor = (req, res, next) => {
  if (req.user.role !== 'SYSTEM') {
    return res.status(403).json({ error: '상담사 권한이 필요합니다' });
  }
  next();
};

// 관리자 권한 확인
const requireAdmin = (req, res, next) => {
  if (req.user.role !== 'ADMIN') {
    return res.status(403).json({ error: '관리자 권한이 필요합니다' });
  }
  next();
};

module.exports = {
  authenticateToken,
  requireCounselor,
  requireAdmin,
};
```

#### 5.3. Express 앱 설정
**파일**: `backend/app.js`
```javascript
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');

// 라우트 임포트
const centerRoutes = require('./routes/centerRoutes');
const reviewRoutes = require('./routes/reviewRoutes');
const adminRoutes = require('./routes/adminRoutes');
const counselRoutes = require('./routes/counselRoutes');

const app = express();

// 미들웨어
app.use(helmet());
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(morgan('dev'));

// 라우트 연결
app.use('/api', centerRoutes);
app.use('/api', reviewRoutes);
app.use('/api', adminRoutes);
app.use('/api', counselRoutes);

// 에러 핸들러
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ error: '서버 오류가 발생했습니다' });
});

module.exports = app;
```

#### 5.4. 서버 시작
**파일**: `backend/server.js`
```javascript
require('dotenv').config();
const app = require('./app');

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
  console.log(`서버가 포트 ${PORT}에서 실행 중입니다`);
});
```

#### 5.5. 환경 변수
**파일**: `backend/.env`
```
# 데이터베이스
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=your_password
DB_NAME=gominsunsak

# JWT
JWT_SECRET=your_jwt_secret_key_here
JWT_EXPIRES_IN=7d

# 서버
PORT=3000
NODE_ENV=development

# Kakao
KAKAO_MAP_API_KEY=your_kakao_map_api_key

# CORS
ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

#### 5.6. package.json
**파일**: `backend/package.json`
```json
{
  "name": "gominsunsak-backend",
  "version": "1.0.0",
  "description": "고민순삭 백엔드 API",
  "main": "server.js",
  "scripts": {
    "start": "node server.js",
    "dev": "nodemon server.js"
  },
  "dependencies": {
    "express": "^4.18.2",
    "mysql2": "^3.6.0",
    "jsonwebtoken": "^9.0.2",
    "bcrypt": "^5.1.1",
    "cors": "^2.8.5",
    "helmet": "^7.0.0",
    "morgan": "^1.10.0",
    "dotenv": "^16.3.1"
  },
  "devDependencies": {
    "nodemon": "^3.0.1"
  }
}
```

---

## 6. DB 스키마 전체

### 📊 전체 테이블 생성 SQL

**파일**: `database_schema.sql`
```sql
-- 사용자 테이블
CREATE TABLE users (
  id INT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  name VARCHAR(100) NOT NULL,
  role ENUM('USER', 'SYSTEM', 'ADMIN') DEFAULT 'USER',
  mbti VARCHAR(4),
  gender VARCHAR(10),
  age INT,
  phone VARCHAR(20),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_email (email),
  INDEX idx_role (role)
);

-- 취업지원 센터 테이블
CREATE TABLE centers (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(200) NOT NULL,
  address VARCHAR(500) NOT NULL,
  phone VARCHAR(20),
  latitude DECIMAL(10, 8) NOT NULL,
  longitude DECIMAL(11, 8) NOT NULL,
  business_hours VARCHAR(200),
  description TEXT,
  website VARCHAR(500),
  category VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_location (latitude, longitude),
  INDEX idx_name (name),
  INDEX idx_category (category)
);

-- 상담 테이블
CREATE TABLE counsels (
  id INT PRIMARY KEY AUTO_INCREMENT,
  counselor_id INT NOT NULL,
  user_id INT NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  detailed_content TEXT,
  status ENUM('scheduled', 'inProgress', 'completed', 'cancelled') DEFAULT 'scheduled',
  counsel_type ENUM('chat', 'video', 'phone') DEFAULT 'chat',
  reservation_date DATETIME NOT NULL,
  started_at DATETIME NULL,
  completed_at DATETIME NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_counselor_status (counselor_id, status),
  INDEX idx_user (user_id),
  INDEX idx_reservation (reservation_date),
  INDEX idx_status (status)
);

-- 상담 메시지 테이블
CREATE TABLE counsel_messages (
  id INT PRIMARY KEY AUTO_INCREMENT,
  counsel_id INT NOT NULL,
  sender_id INT NOT NULL,
  sender_type ENUM('counselor', 'client') NOT NULL,
  message TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (counsel_id) REFERENCES counsels(id) ON DELETE CASCADE,
  FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_counsel (counsel_id),
  INDEX idx_created (created_at)
);

-- 상담 타임라인 테이블
CREATE TABLE counsel_timeline (
  id INT PRIMARY KEY AUTO_INCREMENT,
  counselor_id INT NOT NULL,
  action_type ENUM('request', 'accept', 'start', 'complete', 'cancel') NOT NULL,
  description VARCHAR(500) NOT NULL,
  counsel_id INT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (counsel_id) REFERENCES counsels(id) ON DELETE SET NULL,
  INDEX idx_counselor_created (counselor_id, created_at DESC)
);

-- 리뷰 테이블
CREATE TABLE reviews (
  id INT PRIMARY KEY AUTO_INCREMENT,
  counselor_id INT NOT NULL,
  user_id INT NOT NULL,
  counsel_id INT NOT NULL,
  rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (counsel_id) REFERENCES counsels(id) ON DELETE CASCADE,
  INDEX idx_counselor (counselor_id),
  INDEX idx_user (user_id),
  INDEX idx_rating (rating),
  INDEX idx_created (created_at DESC)
);

-- 리뷰 답글 테이블
CREATE TABLE review_replies (
  id INT PRIMARY KEY AUTO_INCREMENT,
  review_id INT NOT NULL,
  counselor_id INT NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE,
  FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_review (review_id)
);

-- 위험 알림 테이블
CREATE TABLE risk_alerts (
  id INT PRIMARY KEY AUTO_INCREMENT,
  type ENUM('concern', 'career', 'job') NOT NULL,
  counselor_id INT NOT NULL,
  user_id INT NOT NULL,
  counsel_id INT,
  keyword VARCHAR(100) NOT NULL,
  risk_level ENUM('high', 'medium', 'low') DEFAULT 'medium',
  status ENUM('pending', 'resolved') DEFAULT 'pending',
  content TEXT,
  detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  resolved_at TIMESTAMP NULL,
  resolved_by INT NULL,
  resolution_note TEXT NULL,
  FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (counsel_id) REFERENCES counsels(id) ON DELETE SET NULL,
  FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL,
  INDEX idx_status (status),
  INDEX idx_detected (detected_at DESC),
  INDEX idx_keyword (keyword)
);

-- 키워드 통계 테이블
CREATE TABLE keyword_stats (
  id INT PRIMARY KEY AUTO_INCREMENT,
  keyword VARCHAR(100) NOT NULL,
  count INT DEFAULT 1,
  date DATE NOT NULL,
  category VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY unique_keyword_date (keyword, date),
  INDEX idx_date (date),
  INDEX idx_count (count DESC)
);

-- 공지사항 테이블
CREATE TABLE notices (
  id INT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  created_by INT NOT NULL,
  is_important BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_created (created_at DESC)
);

-- 공지사항 읽음 테이블
CREATE TABLE notice_reads (
  id INT PRIMARY KEY AUTO_INCREMENT,
  notice_id INT NOT NULL,
  user_id INT NOT NULL,
  read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (notice_id) REFERENCES notices(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY unique_read (notice_id, user_id),
  INDEX idx_user (user_id)
);
```

---

## 📝 작업 순서 요약

### 1단계: 백엔드 기본 설정
1. `backend/` 폴더에 기본 구조 생성
2. `npm init` 및 필요한 패키지 설치
3. DB 연결 설정 (`config/database.js`)
4. 환경 변수 설정 (`.env`)
5. Express 앱 기본 설정 (`app.js`, `server.js`)

### 2단계: DB 스키마 생성
1. MySQL/MariaDB에 데이터베이스 생성
2. `database_schema.sql` 실행하여 테이블 생성
3. 초기 데이터 삽입 (선택)

### 3단계: 백엔드 API 구현
1. 인증 미들웨어 구현 (`middleware/auth.js`)
2. 각 기능별 라우트 파일 생성
   - `centerRoutes.js`
   - `reviewRoutes.js`
   - `adminRoutes.js`
   - `counselRoutes.js`
3. API 테스트 (Postman, Thunder Client 등)

### 4단계: 프론트엔드 API 연동
1. 각 기능별 API 파일 생성 (`frontend/src/api/`)
2. 컴포넌트에서 더미 데이터 제거
3. `useEffect`로 API 호출 추가
4. 로딩, 에러 처리 추가
5. 기능 테스트

### 5단계: 통합 테스트
1. 전체 기능 통합 테스트
2. 에러 처리 확인
3. 성능 최적화
4. 보안 점검

---

## 🔑 중요 참고사항

1. **환경 변수**: `.env` 파일은 절대 Git에 커밋하지 말 것
2. **보안**: JWT 시크릿 키는 충분히 복잡하게 생성
3. **DB 인덱스**: 쿼리 성능을 위해 적절한 인덱스 생성
4. **에러 핸들링**: 모든 API에 try-catch 구문 필수
5. **검증**: 사용자 입력은 항상 검증 후 DB 저장
6. **로깅**: 중요한 작업은 로그 기록
7. **백업**: 정기적인 DB 백업 필수

---

이 가이드를 따라 단계별로 작업하시면 됩니다! 궁금한 점이 있으면 언제든지 물어보세요.
