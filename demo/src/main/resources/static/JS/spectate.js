// 全局变量
const size = 15;
let board = Array(size).fill(null).map(() => Array(size).fill(null));
let currentPlayer = "black";
let moveHistory = [];
let lastMoveX = -1;
let lastMoveY = -1;
let lastUpdateTime = 0;

// 初始化
document.addEventListener('DOMContentLoaded', function() {
    console.log("观战席页面已加载");
    
    // 初始化棋盘
    drawBoard();
    updateTurnIndicator();
    
    // 绑定刷新按钮
    document.getElementById('refreshButton').addEventListener('click', function() {
        console.log("手动刷新游戏状态");
        fetchGameState();
    });
    
    // 初次获取游戏状态
    fetchGameState();
    
    // 设置定时刷新（每1.5秒）
    setInterval(fetchGameState, 1500);
});

// 绘制初始棋盘
function drawBoard() {
    const boardElement = document.getElementById("chessBoard");
    if (!boardElement) {
        console.error("找不到棋盘元素");
        return;
    }
    
    // 确保棋盘已初始化
    for (let y = 0; y < size; y++) {
        for (let x = 0; x < size; x++) {
            const cell = document.querySelector(`td[data-x="${x}"][data-y="${y}"]`);
            if (cell) {
                // 确保单元格已清空
                cell.innerHTML = '';
            }
        }
    }
    
    console.log("棋盘已初始化");
}

// 获取最新游戏状态
function fetchGameState() {
    fetch('/api/game-state')
        .then(response => {
            if (!response.ok) {
                throw new Error('网络错误: ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            console.log("获取到游戏状态:", data ? data.message : "无数据");
            if (data) {
                updateGameState(data);
            }
        })
        .catch(error => {
            console.error('获取游戏状态出错:', error);
            document.getElementById('statusMessage').textContent = '获取游戏状态失败，请稍后再试';
        });
}

// 更新游戏状态
function updateGameState(data) {
    if (!data.board) {
        console.warn("收到的游戏状态中棋盘数据为空");
        document.getElementById('statusMessage').textContent = '等待游戏开始...';
        return;
    }
    
    // 检查是否有变化
    const hasChanged = checkBoardChanges(data.board);
    
    // 更新棋盘
    board = data.board;
    currentPlayer = data.currentPlayer || "black";
    
    // 更新UI
    updateBoardUI();
    updateTurnIndicator();
    
    // 显示状态消息
    document.getElementById('statusMessage').textContent = data.message || '游戏进行中';
    
    // 如果棋盘有变化，则记录历史
    if (hasChanged) {
        recordMoveHistory();
        console.log("检测到新的落子，已更新历史记录");
    }
    
    lastUpdateTime = Date.now();
}

// 检查棋盘变化并找出新的落子位置
function checkBoardChanges(newBoard) {
    let changed = false;
    let foundNewMove = false;
    
    for (let y = 0; y < size; y++) {
        for (let x = 0; x < size; x++) {
            if (newBoard[y][x] !== board[y][x]) {
                changed = true;
                if (newBoard[y][x] !== null && (board[y][x] === null || board[y][x] === undefined)) {
                    // 找到了新落子
                    lastMoveX = x;
                    lastMoveY = y;
                    foundNewMove = true;
                    console.log("发现新落子位置:", x+1, y+1);
                }
            }
        }
    }
    
    if (foundNewMove) {
        document.getElementById('lastMove').textContent = `(${lastMoveX+1}, ${lastMoveY+1})`;
    }
    
    return changed;
}

// 记录落子历史
function recordMoveHistory() {
    if (lastMoveX >= 0 && lastMoveY >= 0) {
        const pieceValue = board[lastMoveY][lastMoveX];
        if (!pieceValue) {
            console.warn("尝试记录历史时发现落子位置为空");
            return;
        }
        
        const color = pieceValue === "black" ? "黑棋" : "白棋";
        const historyItem = `${moveHistory.length + 1}. ${color} (${lastMoveX+1}, ${lastMoveY+1})`;
        
        moveHistory.push(historyItem);
        
        // 更新历史记录UI
        const historyList = document.getElementById('moveHistory');
        if (historyList) {
            const moveElement = document.createElement('div');
            moveElement.className = 'move-item';
            moveElement.textContent = historyItem;
            historyList.appendChild(moveElement);
            
            // 自动滚动到底部
            historyList.scrollTop = historyList.scrollHeight;
        } else {
            console.error("找不到历史记录列表元素");
        }
    }
}

// 更新棋盘UI
function updateBoardUI() {
    for (let y = 0; y < size; y++) {
        for (let x = 0; x < size; x++) {
            const cell = document.querySelector(`td[data-x="${x}"][data-y="${y}"]`);
            if (!cell) {
                console.warn(`找不到坐标为 (${x}, ${y}) 的单元格`);
                continue;
            }
            
            cell.innerHTML = ''; // 清空单元格
            
            if (board[y][x]) {
                const piece = document.createElement("div");
                piece.className = `piece ${board[y][x]}`;
                
                // 如果是最后一步落子，添加高亮样式
                if (x === lastMoveX && y === lastMoveY) {
                    piece.style.boxShadow = board[y][x] === "black" 
                        ? "0 0 8px 2px rgba(255,0,0,0.7)" 
                        : "0 0 8px 2px rgba(255,0,0,0.7)";
                }
                
                cell.appendChild(piece);
            }
        }
    }
}

// 更新当前回合指示器
function updateTurnIndicator() {
    const indicator = document.getElementById("turnIndicator");
    if (!indicator) {
        console.error("找不到回合指示器元素");
        return;
    }
    
    indicator.style.backgroundColor = currentPlayer === "black" ? "#000" : "#fff";
    indicator.style.boxShadow = currentPlayer === "black" 
        ? "0 0 2px 1px rgba(255,255,255,0.5)" 
        : "0 0 2px 1px rgba(0,0,0,0.5)";
} 