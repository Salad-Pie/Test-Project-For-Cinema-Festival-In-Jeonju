<script setup>
import { inject, reactive, onMounted, watch } from 'vue'
import { apiRoot } from '../config/api'
import { getIdeaContestAuthToken } from '../utils/authStorage'
import { parseErrorResponse } from '../api/client'

const { t, state: globalState, setSafeError, userError } = inject('appContext')

const adminDataState = reactive({
  items: [],
  selectedEntity: 'users',
  entities: [
    { key: 'users', label: '사용자 (User)' },
    { key: 'signatures', label: '서명 (Signature)' },
    { key: 'exhibition-surveys', label: '전시 설문 (ExhibitionSurvey)' },
    { key: 'experience-zone-surveys', label: '체험존 설문 (ExperienceZoneSurvey)' },
    { key: 'sponsorship-applications', label: '후원 신청 (SponsorshipApplication)' },
    { key: 'street-collaboration-reservations', label: '소상공인 협력 (StreetCollaboration)' },
    { key: 'project-recruitments', label: '프로젝트 모집 (ProjectRecruitment)' },
    { key: 'idea-contests', label: '아이디어 공모 (IdeaContest)' },
  ],
  headers: []
})

async function fetchAdminData() {
  globalState.loading = true
  globalState.error = ''
  try {
    const authToken = getIdeaContestAuthToken()
    if (!authToken) throw userError(t('common.loginTokenRequired'))

    const res = await fetch(`${apiRoot}/admin/data/${adminDataState.selectedEntity}`, {
      headers: { Authorization: `Bearer ${authToken}` },
    })
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    
    const data = await res.json()
    adminDataState.items = data
    
    // 데이터가 있으면 헤더 추출
    if (data.length > 0) {
      adminDataState.headers = Object.keys(data[0])
    } else {
      adminDataState.headers = []
    }
  } catch (e) {
    setSafeError(e)
  } finally {
    globalState.loading = false
  }
}

/**
 * 헤더 영문 필드명을 한국어로 변환하는 헬퍼
 */
function translateHeader(header) {
  const mapping = {
    id: '번호',
    email: '이메일',
    role: '권한',
    createdAt: '생성일시',
    updatedAt: '수정일시',
    fullName: '성함',
    phoneNumber: '연락처',
    nickname: '닉네임',
    status: '상태',
    reservationDate: '예약일',
    reservationTime: '예약시간',
    amount: '금액',
    paymentMethodType: '결제수단',
    paymentProviderName: '기관명',
    projectKey: '프로젝트키',
    recognizedText: '인식텍스트',
    koreanName: '한글명',
    englishName: '영문명',
    lastLoginAt: '최근로그인'
  }
  return mapping[header] || header
}

watch(() => adminDataState.selectedEntity, () => {
  fetchAdminData()
})

onMounted(() => {
  fetchAdminData()
})
</script>

<template>
  <section class="card admin-data">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="mb-0">{{ t('adminData.title') }}</h2>
      <button class="btn btn-outline-primary btn-sm" @click="fetchAdminData" :disabled="globalState.loading">
        {{ t('adminSignatures.refresh') }}
      </button>
    </div>

    <div class="mb-4 row align-items-center">
      <div class="col-md-6">
        <label class="form-label font-weight-bold text-secondary mb-2">{{ t('adminData.selectEntity') }}</label>
        <select 
          class="form-select form-select-lg shadow-sm border-primary" 
          v-model="adminDataState.selectedEntity"
          :disabled="globalState.loading"
        >
          <option v-for="entity in adminDataState.entities" :key="entity.key" :value="entity.key">
            {{ entity.label }}
          </option>
        </select>
      </div>
      <div class="col-md-6 text-end pt-4">
        <span class="text-muted small me-2" v-if="adminDataState.items.length > 0">
          총: <strong>{{ adminDataState.items.length }}</strong> 건
        </span>
      </div>
    </div>

    <div class="admin-table-wrap mt-3 shadow-sm rounded">
      <table class="admin-table table table-hover">
        <thead class="thead-light">
          <tr>
            <th v-for="header in adminDataState.headers" :key="header">{{ translateHeader(header) }}</th>
            <th v-if="adminDataState.headers.length === 0">데이터 없음</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, idx) in adminDataState.items" :key="idx">
            <td v-for="header in adminDataState.headers" :key="header" class="text-truncate" style="max-width: 200px;" :title="item[header]">
              {{ item[header] !== null ? item[header] : '-' }}
            </td>
          </tr>
          <tr v-if="adminDataState.items.length === 0">
            <td :colspan="adminDataState.headers.length || 1" class="text-center py-5">
              <div class="text-muted">
                <i class="bi bi-inbox display-4 d-block mb-2"></i>
                {{ t('adminReservations.empty') }}
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
.admin-data {
  padding: 2rem;
}
.gap-2 {
  gap: 0.5rem;
}
.admin-table-wrap {
  overflow-x: auto;
  background: white;
}
.admin-table th {
  background-color: #f8f9fa;
  position: sticky;
  top: 0;
  z-index: 10;
}
.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
