<script setup>
import { reactive, computed, watch, inject } from 'vue'
import { apiRoot } from '../config/api'
import { combineAddress, formatPhoneInput } from '../utils/format'
import { parseErrorResponse } from '../api/client'

const { t, state: globalState, setSafeError, goToSuccessPage, openAddressSearch, ideaPosterUrl } = inject('appContext')

const customPaymentProviderValue = '__CUSTOM__'
const sponsorshipPaymentProviderOptionsByType = {
  BANK: ['KB', 'SHINHAN', 'WOORI', 'HANA', 'NH', 'IBK', 'KAKAO BANK', 'TOSS BANK', 'K BANK'],
  PAYMENT: ['KAKAO PAY', 'NAVER PAY', 'TOSS PAY', 'PAYCO', 'PayPal'],
  CARD: ['SHINHAN CARD', 'SAMSUNG CARD', 'KB CARD', 'HYUNDAI CARD', 'LOTTE CARD', 'WOORI CARD', 'HANA CARD', 'BC CARD', 'Visa', 'Mastercard'],
}

const sponsorshipState = reactive({
  name: '',
  phoneNumber: '',
  bankAccount: '',
  paymentMethodType: 'BANK',
  paymentProviderPreset: '',
  paymentProviderName: '',
  amount: '',
  address: '',
  addressDetail: '',
})

const sponsorshipPaymentProviderOptions = computed(
  () => sponsorshipPaymentProviderOptionsByType[sponsorshipState.paymentMethodType] || []
)

const isCustomSponsorshipPaymentProvider = computed(
  () => sponsorshipState.paymentProviderPreset === customPaymentProviderValue
)

watch(
  () => sponsorshipState.paymentMethodType,
  () => {
    sponsorshipState.paymentProviderPreset = ''
    sponsorshipState.paymentProviderName = ''
  },
)

watch(
  () => sponsorshipState.paymentProviderPreset,
  (nextProvider) => {
    if (nextProvider !== customPaymentProviderValue) {
      sponsorshipState.paymentProviderName = nextProvider
    } else {
      sponsorshipState.paymentProviderName = ''
    }
  },
)

function onSponsorshipPhoneInput(event) {
  sponsorshipState.phoneNumber = formatPhoneInput(event)
}

async function submitSponsorship() {
  globalState.loading = true
  globalState.error = ''
  try {
    const payload = {
      name: sponsorshipState.name,
      phoneNumber: sponsorshipState.phoneNumber,
      bankAccount: sponsorshipState.bankAccount,
      paymentMethodType: sponsorshipState.paymentMethodType,
      paymentProviderName: sponsorshipState.paymentProviderName,
      amount: Number(sponsorshipState.amount),
      address: combineAddress(sponsorshipState.address, sponsorshipState.addressDetail),
    }

    const res = await fetch(`${apiRoot}/sponsorship-applications`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })

    if (!res.ok) {
      const errorMsg = await parseErrorResponse(res, t('common.requestFailed'))
      throw new Error(errorMsg)
    }

    await res.json()
    goToSuccessPage(t('sponsorship.submitted'))
  } catch (e) {
    setSafeError(e)
  } finally {
    globalState.loading = false
  }
}
</script>

<template>
  <section class="idea-shell">
    <div class="card idea-pane">
      <div class="idea-form">
        <h2>{{ t('sponsorship.title') }}</h2>
        <div class="grid">
          <label>{{ t('sponsorship.name') }} <input v-model="sponsorshipState.name" type="text" :placeholder="t('common.placeholders.name')" /></label>
          <label>{{ t('sponsorship.phone') }} <input v-model="sponsorshipState.phoneNumber" type="text" :placeholder="t('common.placeholders.phone')" @input="onSponsorshipPhoneInput" /></label>
          <label>{{ t('sponsorship.paymentMethodType') }}
            <select v-model="sponsorshipState.paymentMethodType">
              <option value="BANK">{{ t('sponsorship.paymentMethods.bank') }}</option>
              <option value="PAYMENT">{{ t('sponsorship.paymentMethods.payment') }}</option>
              <option value="CARD">{{ t('sponsorship.paymentMethods.card') }}</option>
            </select>
          </label>
          <label>{{ t('sponsorship.paymentProviderName') }}
            <select v-model="sponsorshipState.paymentProviderPreset">
              <option disabled value="">{{ t('common.placeholders.paymentProviderName') }}</option>
              <option v-for="provider in sponsorshipPaymentProviderOptions" :key="provider" :value="provider">{{ provider }}</option>
              <option :value="customPaymentProviderValue">{{ t('sponsorship.paymentProviderCustom') }}</option>
            </select>
          </label>
          <label v-if="isCustomSponsorshipPaymentProvider">{{ t('sponsorship.paymentProviderCustom') }} <input v-model="sponsorshipState.paymentProviderName" type="text" :placeholder="t('common.placeholders.paymentProviderName')" /></label>
          <label>{{ t('sponsorship.bankAccount') }} <input v-model="sponsorshipState.bankAccount" type="text" :placeholder="t('common.placeholders.bankAccount')" /></label>
          <label>{{ t('sponsorship.amount') }} <input v-model="sponsorshipState.amount" type="number" min="1" :placeholder="t('common.placeholders.amount')" /></label>
        </div>
        <label>{{ t('sponsorship.address') }}</label>
        <div class="address-field">
          <div class="address-search-row">
            <input v-model="sponsorshipState.address" type="text" readonly :placeholder="t('common.baseAddress')" />
            <button type="button" :disabled="globalState.loading" @click="openAddressSearch(sponsorshipState)">{{ t('common.searchAddress') }}</button>
          </div>
          <input v-model="sponsorshipState.addressDetail" type="text" :placeholder="t('common.detailAddress')" />
        </div>
        <button :disabled="globalState.loading" @click="submitSponsorship">{{ t('sponsorship.submit') }}</button>
      </div>
    </div>
    <div class="card idea-pane">
      <div class="idea-poster">
        <img :src="ideaPosterUrl" alt="Poster Placeholder" />
      </div>
    </div>
  </section>
</template>
