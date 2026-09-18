// Markdown 解析器（AI 回答、来源分块预览共用）：禁原生 HTML，规避 XSS
import MarkdownIt from 'markdown-it'

const md = new MarkdownIt({ html: false, linkify: true })

/** 将 markdown 字符串渲染为 HTML；非法/空输入返回空串 */
export function renderMarkdown(content: string): string {
  if (!content) return ''
  return md.render(content)
}

export default md