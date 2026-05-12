<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

onMounted(() => {
  router.replace({
    path: '/dashboard/message/list',
    query: { ...route.query }
  })
})
</script>

<template>
  <div class="chat-redirect" />
</template>

<style scoped>
.chat-redirect {
  min-height: 80px;
}
</style>
