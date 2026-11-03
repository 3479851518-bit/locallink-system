const { app, BrowserWindow, ipcMain } = require('electron')
const path = require('path')
const { spawn } = require('child_process')

let mainWindow
let backendProcess

// 后端服务端口
const BACKEND_PORT = 8080
const BACKEND_URL = `http://localhost:${BACKEND_PORT}`

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    minWidth: 800,
    minHeight: 600,
    title: 'LocalLink - 局域网即时通讯系统',
    icon: path.join(__dirname, 'icon.ico'),
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      enableRemoteModule: false,
      preload: path.join(__dirname, 'preload.cjs')
    }
  })

  if (process.env.NODE_ENV === 'development') {
    mainWindow.loadURL('http://localhost:5173')
    mainWindow.webContents.openDevTools()
  } else {
    mainWindow.loadFile(path.join(__dirname, '../dist/index.html'))
  }

  mainWindow.on('closed', () => {
    mainWindow = null
  })
}

function resolveBackendJar() {
  const fs = require('fs')

  if (app.isPackaged) {
    const backendDir = path.join(process.resourcesPath, 'backend')
    if (!fs.existsSync(backendDir)) {
      return null
    }
    const jarFile = fs.readdirSync(backendDir).find(f => f.endsWith('.jar'))
    if (!jarFile) {
      return null
    }
    return { jarPath: path.join(backendDir, jarFile), cwd: backendDir }
  }

  const projectRoot = path.join(__dirname, '..')
  const targetDir = path.join(projectRoot, 'target')
  if (!fs.existsSync(targetDir)) {
    return null
  }
  const jarFile = fs.readdirSync(targetDir).find(f => f.endsWith('.jar'))
  if (!jarFile) {
    return null
  }
  return { jarPath: path.join(targetDir, jarFile), cwd: projectRoot }
}

function startBackend() {
  console.log('Starting backend...')

  const fs = require('fs')
  const resolved = resolveBackendJar()
  if (!resolved) {
    console.log('Backend jar not found')
    return false
  }

  backendProcess = spawn('java', ['-jar', resolved.jarPath], {
    cwd: resolved.cwd,
    stdio: ['pipe', 'pipe', 'pipe']
  })

  backendProcess.stdout.on('data', (data) => {
    const output = data.toString()
    console.log(output)

    if (output.includes('Started LocallinkSystemApplication')) {
      console.log('后端服务启动成功')
      if (mainWindow) {
        mainWindow.webContents.send('backend-ready', BACKEND_URL)
      }
    }
  })

  backendProcess.stderr.on('data', (data) => {
    console.error(`后端错误: ${data}`)
  })

  backendProcess.on('close', (code) => {
    console.log(`后端服务已退出，退出码: ${code}`)
    if (mainWindow) {
      mainWindow.webContents.send('backend-exited', code)
    }
  })

  return true
}

function stopBackend() {
  if (backendProcess) {
    console.log('正在停止后端服务...')
    backendProcess.kill()
    backendProcess = null
  }
}

app.whenReady().then(() => {
  createWindow()

  setTimeout(() => {
    startBackend()
  }, 1000)

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow()
    }
  })
})

app.on('window-all-closed', () => {
  stopBackend()
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

app.on('will-quit', () => {
  stopBackend()
})

ipcMain.handle('get-backend-url', () => {
  return BACKEND_URL
})

ipcMain.handle('check-backend', async () => {
  try {
    const response = await fetch(`${BACKEND_URL}/api/user/info/999`)
    return response.ok
  } catch (e) {
    return false
  }
})
