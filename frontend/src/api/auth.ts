import request from './request'
import type { Result } from '@/types'

/** 登录，成功 data 里是 JWT */
export function login(username: string, password: string) {
  return request.post<Result<string>>('/users/login', { username, password })
}

/** 注册 */
export function register(username: string, password: string) {
  return request.post<Result<string>>('/users/register', { username, password })
}
