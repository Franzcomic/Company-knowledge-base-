import { http } from '@/utils/request'

/** 统计时间范围：7d/30d/90d/all（后端默认 7d，非法值兜底 7d） */
export type StatsRange = '7d' | '30d' | '90d' | 'all'

/** 趋势点（GET /api/stats/overview 内嵌） */
export interface TrendPoint {
  date: string // ISO 日期 yyyy-MM-dd
  total: number
}

/** 统计总览（GET /api/stats/overview）：avgResponseMs / satisfaction 无数据时为 null（后端 NON_NULL 不输出） */
export interface StatsOverview {
  total: number // 问题总数（USER 消息数）
  answered: number // 已回答数
  notAnswered: number // 拒答数
  hitRate: number // 命中率 answered/total
  avgResponseMs?: number | null // 平均响应耗时(ms)
  satisfaction?: number | null // 满意率 UP/(UP+DOWN)
  trend: TrendPoint[] // 每日问题趋势（逐日补 0）
}

/** 热门问题（GET /api/stats/hot-questions） */
export interface HotQuestion {
  question: string
  count: number
}

/** 热门知识库（GET /api/stats/hot-kb） */
export interface HotKb {
  kbName: string
  count: number
}

/** 部门分布（GET /api/stats/departments） */
export interface DepartmentStat {
  deptName: string
  count: number
}

/* ---------------- 接口（全部仅 SYS_ADMIN，其余角色后端 403） ---------------- */

/** 统计总览：问题量 / 命中率 / 响应时间 / 满意率 / 逐日趋势 */
export function getStatsOverview(range: StatsRange = '7d') {
  return http.get<StatsOverview>('/stats/overview', { range })
}

/** 热门问题排名（按问题原文分组计数，Top N） */
export function getHotQuestions(range: StatsRange = '7d', limit = 10) {
  return http.get<HotQuestion[]>('/stats/hot-questions', { range, limit })
}

/** 热门知识库（按来源被引次数统计） */
export function getHotKb(range: StatsRange = '7d', limit = 10) {
  return http.get<HotKb[]>('/stats/hot-kb', { range, limit })
}

/** 部门分布（按会话发起部门统计问题数） */
export function getDepartmentStats(range: StatsRange = '7d') {
  return http.get<DepartmentStat[]>('/stats/departments', { range })
}

/** 未解决问题（被拒答 answered=0 的问题原文） */
export function getUnsolvedQuestions(range: StatsRange = '7d', limit = 10) {
  return http.get<string[]>('/stats/unsolved', { range, limit })
}
