<script setup>
import { inject, reactive, onMounted, watch } from 'vue'
import { apiRoot } from '../config/api'
import { getIdeaContestAuthToken } from '../utils/authStorage'
import { parseErrorResponse } from '../api/client'

/**
 * 전역 컨텍스트 주입
 */
const { t, state: globalState, setSafeError } = inject('appContext')

/**
 * 다국어 편집 상태
 */
const editorState = reactive({
  selectedLang: 'ko',
  messages: [], // { key, content }
  loading: false,
  saving: false
})

/**
 * 현재 선택된 언어의 모든 메시지 로드
 */
async function fetchMessages() {
  editorState.loading = true
  try {
    const res = await fetch(`${apiRoot}/i18n/messages/${editorState.selectedLang}`)
    if (!res.ok) throw new Error(t('common.requestFailed'))
    
    const data = await res.json()
    // 프론트엔드의 기본 메시지들과 병합하기 위해 현재 사용 중인 모든 키를 추출할 수도 있지만, 
    // 여기서는 DB에 저장된 내역만 편집 대상으로 노출합니다.
    editorState.messages = Object.entries(data).map(([key, content]) => ({
      key,
      content
    }))
    
    // 만약 DB에 데이터가 하나도 없다면 안내 메시지 노출
    if (editorState.messages.length === 0) {
      globalState.message = 'No messages found in DB for this locale.'
    }
  } catch (e) {
    setSafeError(e)
  } finally {
    editorState.loading = false
  }
}

/**
 * 특정 키의 번역 사항 저장
 */
async function saveMessage(key, content) {
  editorState.saving = true
  try {
    const authToken = getIdeaContestAuthToken()
    const res = await fetch(`${apiRoot}/i18n/admin/update`, {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json',
        Authorization: `Bearer ${authToken}` 
      },
      body: JSON.stringify({
        key,
        locale: editorState.selectedLang,
        content
      })
    })
    
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    
    globalState.message = t('adminI18nEditor.saveSuccess') || 'Translation updated successfully.'
  } catch (e) {
    setSafeError(e)
  } finally {
    editorState.saving = false
  }
}

/**
 * 전체 저장 (루프 돌며 수정된 항목들 처리)
 */
async function saveAll() {
  for (const msg of editorState.messages) {
    await saveMessage(msg.key, msg.content)
  }
  globalState.message = 'All changes saved to DB.'
}

// 언어 변경 시 자동 로드
watch(() => editorState.selectedLang, fetchMessages)

onMounted(() => {
  fetchMessages()
})
</script>

<template>
  <section class="card admin-i18n-editor">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>{{ t('adminI18nEditor.title') }}</h2>
      <button class="btn btn-sm btn-outline-primary" @click="fetchMessages" :disabled="editorState.loading">
        {{ t('common.refresh') || 'Refresh' }}
      </button>
    </div>
    
    <div class="alert alert-info">
      <i class="bi bi-info-circle me-2"></i>
      {{ t('adminI18nEditor.info') || 'Only messages stored in the database are shown here. Updates will reflect in real-time for users.' }}
    </div>

    <!-- 언어 선택 셀렉트 박스 -->
    <div class="mb-4">
      <label class="form-label font-weight-bold">{{ t('lang.label') }}</label>
      <select v-model="editorState.selectedLang" class="form-select w-25">
        <option value="ko">한국어 (KO)</option>
        <option value="en">English (EN)</option>
        <option value="zh">中文 (ZH)</option>
        <option value="ja">日本語 (JA)</option>
      </select>
    </div>

    <!-- 번역 키/값 편집 테이블 -->
    <div class="admin-table-wrap mb-4" style="max-height: 500px; overflow-y: auto;">
      <table class="admin-table">
        <thead>
          <tr>
            <th style="width: 30%">Key</th>
            <th style="width: 70%">Value (Real-time Editing)</th>
          </tr>
        </thead>
        <tbody v-if="editorState.messages.length">
          <tr v-for="item in editorState.messages" :key="item.key">
            <td><small class="text-muted">{{ item.key }}</small></td>
            <td>
              <div class="input-group">
                <input type="text" v-model="item.content" class="form-control" />
                <button class="btn btn-outline-primary btn-sm" @click="saveMessage(item.key, item.content)" :disabled="editorState.saving">
                  {{ t('common.save') || 'Save' }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
        <tbody v-else>
          <tr>
            <td colspan="2" class="text-center py-5 text-muted">
              {{ editorState.loading ? 'Loading...' : 'No translations found in database for this language.' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 일괄 저장 액션 (추가 편의 기능) -->
    <div class="d-flex gap-2">
      <button class="btn btn-success flex-grow-1" @click="saveAll" :disabled="editorState.saving || !editorState.messages.length">
        {{ t('adminI18nEditor.saveAll') || 'Save All Changes' }}
      </button>
    </div>
  </section>
</template>

<style scoped>
.admin-i18n-editor .admin-table td {
  vertical-align: middle;
}
</style>
