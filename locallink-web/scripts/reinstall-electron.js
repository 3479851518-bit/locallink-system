/**
 * 使用国内镜像重新安装 Electron（GitHub 下载失败时执行）
 * 用法: npm run electron:reinstall
 */
import { execSync } from 'child_process'
import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const root = path.join(__dirname, '..')

const env = {
  ...process.env,
  ELECTRON_MIRROR: 'https://npmmirror.com/mirrors/electron/',
  ELECTRON_BUILDER_BINARIES_MIRROR: 'https://npmmirror.com/mirrors/electron-builder-binaries/'
}

const electronDir = path.join(root, 'node_modules', 'electron')
if (fs.existsSync(electronDir)) {
  console.log('正在删除旧的 electron 模块...')
  fs.rmSync(electronDir, { recursive: true, force: true })
}

console.log('正在通过 npmmirror 重新安装 electron...')
execSync('npm install electron@^28.0.0 --save-dev', {
  cwd: root,
  env,
  stdio: 'inherit',
  shell: true
})

console.log('完成。请执行: npm run electron:build-win')
