<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { login, register } from '@/api/auth'

const router = useRouter()
const auth = useAuthStore()

const isRegister = ref(false)
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  if (!username.value || !password.value) {
    error.value = '用户名和密码不能为空'
    return
  }
  error.value = ''
  loading.value = true
  try {
    if (isRegister.value) {
      const { data } = await register(username.value, password.value)
      if (data.code !== 0) {
        error.value = data.msg || '注册失败'
        return
      }
    }
    const { data } = await login(username.value, password.value)
    if (data.code === 0 && data.data) {
      auth.setAuth(data.data, username.value)
      router.push('/chat')
    } else {
      error.value = data.msg || '登录失败'
    }
  } catch (e: unknown) {
    error.value = '网络错误：' + (e instanceof Error ? e.message : String(e))
  } finally {
    loading.value = false
  }
}

function switchMode(reg: boolean) {
  isRegister.value = reg
  error.value = ''
}
</script>

<template>
  <div class="auth-card">
    <div class="logo">H</div>
    <h1>旅行智能客服</h1>
    <div class="tip">订单查询 · 改签办理 · 政策咨询</div>

    <div class="tabs">
      <button :class="{ active: !isRegister }" @click="switchMode(false)">登录</button>
      <button :class="{ active: isRegister }" @click="switchMode(true)">注册</button>
    </div>

    <div class="field">
      <label>用户名</label>
      <input v-model.trim="username" placeholder="请输入用户名" @keyup.enter="submit" />
    </div>
    <div class="field">
      <label>密码</label>
      <input v-model="password" type="password" placeholder="请输入密码" @keyup.enter="submit" />
    </div>

    <button class="btn-primary" :disabled="loading" @click="submit">
      {{ loading ? '处理中...' : isRegister ? '注册并登录' : '登 录' }}
    </button>
    <div class="err">{{ error }}</div>
  </div>
</template>

<style scoped>
.auth-card {
  width: min(400px, 92vw);
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.35);
  padding: 40px 34px;
  animation: rise 0.3s ease;
}
@keyframes rise {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: none; }
}
.logo {
  width: 54px;
  height: 54px;
  margin: 0 auto;
  border-radius: 12px;
  background: linear-gradient(135deg, #c8a25b, #b08d44);
  color: #14213d;
  font-family: Georgia, serif;
  font-weight: 800;
  font-size: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}
h1 { text-align: center; font-size: 21px; margin: 14px 0 4px; color: #1b2b4b; font-family: Georgia, 'Microsoft YaHei', serif; }
.tip { text-align: center; color: #a59b86; font-size: 12.5px; margin-bottom: 26px; letter-spacing: 0.5px; }

.tabs { display: flex; background: #f3f0e9; border-radius: 10px; padding: 4px; margin-bottom: 22px; }
.tabs button {
  flex: 1;
  border: none;
  background: transparent;
  padding: 9px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #8a8270;
  transition: 0.15s;
}
.tabs button.active { background: #fff; color: #14213d; font-weight: 600; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08); }

.field { margin-bottom: 16px; }
.field label { display: block; font-size: 13px; color: #5a5346; margin-bottom: 6px; }
.field input {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid #ded6c8;
  border-radius: 10px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.15s;
  background: #fcfbf8;
}
.field input:focus { border-color: #c8a25b; }

.btn-primary {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 10px;
  background: #14213d;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  cursor: pointer;
  transition: background 0.15s;
  margin-top: 6px;
}
.btn-primary:hover:not(:disabled) { background: #1b2b4b; }
.btn-primary:disabled { background: #c9c3b6; cursor: not-allowed; }

.err { color: #c0392b; font-size: 13px; margin-top: 12px; text-align: center; min-height: 18px; }
</style>
