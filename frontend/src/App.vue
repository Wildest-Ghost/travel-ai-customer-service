<script setup lang="ts"></script>

<template>
  <router-view />
</template>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }

html, body, #app {
  height: 100%;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
               'Microsoft YaHei', sans-serif;
  background: linear-gradient(135deg, #0f1b30 0%, #243b55 100%);
  color: #1f2937;
}

#app {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
