import { useI18n } from 'vue-i18n'

/**
 * 주소 검색 API 연동을 관리하는 Composable 함수
 * @param {object} globalState - useGlobalState에서 반환된 객체
 */
export function useAddress(globalState) {
  const { t } = useI18n()
  const { state } = globalState

  /**
   * 다음 우체국 주소 검색 API 팝업을 여는 함수
   * @param {object} target - 검색 결과를 담을 반응형 객체 (address, addressDetail 필드 필요)
   */
  const openAddressSearch = (target) => {
    // Daum 스크립트 로드 여부 확인
    if (!window.daum?.Postcode) {
      state.error = t('common.addressApiUnavailable')
      return
    }

    // 주소 검색 팝업 실행
    new window.daum.Postcode({
      oncomplete(data) {
        // 도로명 주소 또는 지번 주소 중 선택된 결과 사용
        const selectedAddress = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress
        target.address = selectedAddress || data.address || ''
        target.addressDetail = '' // 상세 주소 초기화
        state.error = ''
      },
    }).open()
  }

  return {
    openAddressSearch
  }
}
