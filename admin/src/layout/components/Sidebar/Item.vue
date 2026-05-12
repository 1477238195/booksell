<template>
  <el-icon v-if="props.icon">
    <svg-icon :name="props.icon" />
  </el-icon>
</template>

<script setup>
const props = defineProps({
  icon: {
    type: String,
    default: ''
  }
})
</script>
