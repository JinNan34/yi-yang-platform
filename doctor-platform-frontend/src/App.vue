<template>
  <router-view />
</template>

<script setup>
</script>

<style>
:root {
  --app-primary: #1a5f7a;
  --app-bg-page: #f5f7fa;
  --app-radius-card: 4px;
}
html,
body,
#app {
  margin: 0;
  height: 100%;
}
@media (max-width: 640px) {
  .el-overlay-dialog .el-dialog {
    width: min(92vw, 100%) !important;
  }
}
</style>
