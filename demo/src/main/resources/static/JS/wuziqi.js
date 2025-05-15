// 全局变量定义
const size = 15;
let board = Array(size).fill(null).map(() => Array(size).fill(null));
let currentPlayer = "black";
let isAIMode = false; // 控制是否为AI模式
let gameOver = false; // 控制游戏是否结束

// 日志辅助函数
function log(level, message, data) {
    const timestamp = new Date().toISOString();
    if (data) {
        console[level](`[${timestamp}] ${message}`, data);
    } else {
        console[level](`[${timestamp}] ${message}`);
    }
}

// 错误处理辅助函数
function safeExecute(funcName, func) {
    try {
        return func();
    } catch (error) {
        log('error', `执行${funcName}时出错:`, error);
        return null;
    }
}

// 更新当前回合指示器
function updateTurnIndicator() {
    safeExecute('updateTurnIndicator', () => {
        const indicator = document.getElementById("turnIndicator");
        if (!indicator) {
            log('error', "未找到回合指示器元素");
            return;
        }
        
        // 移除所有旧的类
        indicator.classList.remove("black", "white");
        // 添加当前玩家的类
        indicator.classList.add(currentPlayer);
        log('info', `更新回合指示器为 ${currentPlayer}`);
    });
}

// 切换游戏模式
function toggleGameMode() {
    safeExecute('toggleGameMode', () => {
        isAIMode = !isAIMode;
        const modeButton = document.getElementById('toggleModeButton');
        if (modeButton) {
            modeButton.textContent = isAIMode ? "切换到玩家对战" : "切换到AI对战";
        }
    
        // 更新当前模式文字
        const gameModeElement = document.getElementById('gameMode');
        if (gameModeElement) {
            gameModeElement.textContent = isAIMode ? "当前模式：AI 对战" : "当前模式：玩家对战";
        }
    
        // 通知后端切换 AI 模式
        fetch('/toggle-ai')
            .then(res => res.json())
            .then(data => log('info', 'AI 模式状态：', data))
            .catch(error => log('error', 'AI模式切换失败:', error));
    
        resetGame();
        log('info', `游戏模式切换为 ${isAIMode ? 'AI对战' : '玩家对战'}`);
    });
}

// 评估棋盘位置得分
function evaluatePosition(x, y, player) {
    return safeExecute('evaluatePosition', () => {
        const directions = [
            [1, 0], [0, 1], [1, 1], [1, -1]  // 水平、垂直、两个对角线
        ];
        let score = 0;
    
        directions.forEach(([dx, dy]) => {
            let count = 1;
            let blocked = 0;
            
            // 向两个方向检查
            for (let dir = -1; dir <= 1; dir += 2) {
                let i = 1;
                while (true) {
                    let newX = x + dx * dir * i;
                    let newY = y + dy * dir * i;
                    
                    if (newX < 0 || newX >= size || newY < 0 || newY >= size) {
                        blocked++;
                        break;
                    }
                    
                    if (board[newY][newX] === player) {
                        count++;
                        i++;
                    } else if (board[newY][newX] === null) {
                        break;
                    } else {
                        blocked++;
                        break;
                    }
                }
            }
    
            // 根据连子数和封闭状态评分
            if (count >= 5) score += 100000;
            else if (count === 4 && blocked === 0) score += 10000;
            else if (count === 4 && blocked === 1) score += 1000;
            else if (count === 3 && blocked === 0) score += 1000;
            else if (count === 3 && blocked === 1) score += 100;
            else if (count === 2 && blocked === 0) score += 100;
        });
    
        return score;
    }) || 0;
}

// AI移动逻辑
function aiMove() {
    safeExecute('aiMove', () => {
        if (gameOver) return;
        
        let bestScore = -Infinity;
        let bestMove = null;
        
        // 遍历所有可能的位置
        for (let y = 0; y < size; y++) {
            for (let x = 0; x < size; x++) {
                if (board[y][x] === null) {
                    // 评估AI落子
                    let scoreAI = evaluatePosition(x, y, "white");
                    // 评估玩家落子（防守）
                    let scorePlayer = evaluatePosition(x, y, "black");
                    // 综合评分
                    let score = scoreAI + scorePlayer * 0.8;
    
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = {x, y};
                    }
                }
            }
        }
    
        if (bestMove) {
            makeMove(bestMove.x, bestMove.y);
            log('info', `AI选择移动到位置(${bestMove.x}, ${bestMove.y})`);
        } else {
            // 如果没有最佳移动，可以尝试获取后端的AI移动
            fetch('/ai-move')
                .then(response => {
                    if (!response.ok) {
                        throw new Error('AI移动请求失败');
                    }
                    return response.json();
                })
                .then(data => {
                    if (data.success && data.aiMove) {
                        makeMove(data.aiMove[0], data.aiMove[1]);
                        log('info', `从后端获取AI移动到位置(${data.aiMove[0]}, ${data.aiMove[1]})`);
                    } else {
                        log('warn', 'AI无法确定移动位置');
                    }
                })
                .catch(error => log('error', 'AI移动错误:', error));
        }
    });
}

// 处理落子
function makeMove(x, y) {
    safeExecute('makeMove', () => {
        // 验证参数
        if (typeof x !== 'number' || typeof y !== 'number') {
            log('error', `落子位置无效，x=${x}, y=${y}`);
            return;
        }
        
        if (x < 0 || x >= size || y < 0 || y >= size) {
            log('error', `落子位置超出棋盘范围，x=${x}, y=${y}`);
            return;
        }
        
        // 检查游戏状态
        if (gameOver) {
            log('warn', "游戏已结束，无法落子");
            return;
        }
        
        // 检查目标位置
        if (board[y][x] !== null) {
            log('warn', `位置(${x},${y})已有棋子，无法落子`);
            return;
        }
        
        log('info', `尝试在位置(${x},${y})落子，当前玩家: ${currentPlayer}`);
        
        // 发送请求到后端
        fetch('/move', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `x=${x}&y=${y}&player=${currentPlayer}`
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('服务器响应错误: ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            log('info', "服务器响应:", data);
            if (data.success) {
                // 更新本地棋盘状态
                if (data.board && Array.isArray(data.board)) {
                    board = data.board;
                    log('info', "棋盘状态已更新");
                } else {
                    log('error', '服务器返回的棋盘数据无效');
                    return;
                }
                
                if (data.currentPlayer) {
                    currentPlayer = data.currentPlayer;
                    log('info', `当前玩家设置为 ${currentPlayer}`);
                }
                
                // 更新UI
                updateBoardUI();
                updateTurnIndicator();
                
                // 检查胜负
                if (checkWin(x, y, board[y][x])) {
                    setTimeout(() => {
                        alert(`${board[y][x] === "black" ? "黑棋" : "白棋"} 获胜！`);
                        gameOver = true;
                    }, 100);
                    log('info', `${board[y][x]} 获胜`);
                    return;
                }
                
                // 如果是AI模式且轮到白棋(AI)
                if (isAIMode && currentPlayer === "white" && !gameOver) {
                    log('info', "轮到AI落子");
                    setTimeout(aiMove, 500);
                }
            } else {
                log('error', '落子失败:', data.message);
                alert("落子失败: " + data.message);
            }
        })
        .catch(error => {
            log('error', '落子请求错误:', error);
            alert("请求错误: " + error.message);
        });
    });
}

// 更新棋盘UI
function updateBoardUI() {
    safeExecute('updateBoardUI', () => {
        log('info', "正在更新棋盘UI");
        
        // 安全检查：确保board是有效的二维数组
        if (!board || !Array.isArray(board)) {
            log('error', "无效的棋盘数据，board不是数组");
            return;
        }
        
        for (let y = 0; y < board.length; y++) {
            if (!Array.isArray(board[y])) {
                log('error', `无效的棋盘行数据，board[${y}]不是数组`);
                continue;
            }
            
            for (let x = 0; x < board[y].length; x++) {
                const cell = document.querySelector(`td[data-x="${x}"][data-y="${y}"]`);
                if (!cell) {
                    log('error', `未找到坐标为(${x},${y})的单元格`);
                    continue;
                }
                
                cell.innerHTML = ''; // 清空单元格
                
                if (board[y][x]) {
                    // 创建棋子元素
                    const piece = document.createElement("div");
                    // 使用与CSS中一致的类名
                    piece.className = `piece ${board[y][x]}`;
                    cell.appendChild(piece);
                }
            }
        }
    });
}

// 绘制棋盘
function drawBoard() {
    safeExecute('drawBoard', () => {
        const boardElement = document.getElementById("chessBoard");
        if (!boardElement) {
            log('error', "未找到棋盘元素");
            return;
        }
        
        log('info', "开始绘制棋盘");
        boardElement.innerHTML = '';
    
        for (let y = 0; y < size; y++) {
            const row = document.createElement("tr");
            for (let x = 0; x < size; x++) {
                const cell = document.createElement("td");
                cell.setAttribute("data-x", x);
                cell.setAttribute("data-y", y);
                cell.classList.add("cell");
    
                // 绑定点击事件
                cell.addEventListener("click", () => {
                    log('info', `单元格(${x},${y})被点击`);
                    if (!gameOver && (!isAIMode || currentPlayer === "black")){
                        makeMove(x, y);
                    }
                });
    
                if (board[y][x]) {
                    const piece = document.createElement("div");
                    // 使用与CSS中一致的类名
                    piece.className = `piece ${board[y][x]}`;
                    cell.appendChild(piece);
                }
    
                row.appendChild(cell);
            }
            boardElement.appendChild(row);
        }
        log('info', "棋盘绘制完成");
    });
}

// 重置游戏
function resetGame() {
    safeExecute('resetGame', () => {
        log('info', "正在重置游戏");
        fetch('/reset', { method: 'GET' })
            .then(response => response.text())
            .then(data => {
                log('info', "重置游戏响应:", data);
                board = Array(size).fill(null).map(() => Array(size).fill(null));
                currentPlayer = "black";
                gameOver = false;
                drawBoard();
                updateTurnIndicator();
                log('info', "游戏已重置");
            })
            .catch(error => log('error', '重置游戏错误:', error));
    });
}

// 检查胜利条件
function checkWin(x, y, player) {
    return safeExecute('checkWin', () => {
        if (!player) {
            log('error', `检查胜利时玩家无效: ${player}`);
            return false;
        }
        
        const directions = [[1,0], [0,1], [1,1], [1,-1]];
        return directions.some(([dx, dy]) => {
            let count = 1;
            // 正向检查
            for (let i = 1; i < 5; i++) {
                if (y + i * dy < 0 || y + i * dy >= board.length || 
                    x + i * dx < 0 || x + i * dx >= board[0].length || 
                    board[y + i * dy][x + i * dx] !== player) break;
                count++;
            }
            // 反向检查
            for (let i = 1; i < 5; i++) {
                if (y - i * dy < 0 || y - i * dy >= board.length || 
                    x - i * dx < 0 || x - i * dx >= board[0].length || 
                    board[y - i * dy][x - i * dx] !== player) break;
                count++;
            }
            const win = count >= 5;
            if (win) {
                log('info', `检测到${player}有${count}个连子，胜利条件满足`);
            }
            return win;
        });
    }) || false;
}

// 加载记录列表
function loadRecords() {
    safeExecute('loadRecords', () => {
        log('info', "正在加载游戏记录列表");
        const recordsDiv = document.getElementById('gameRecords');
        if (!recordsDiv) {
            log('error', "未找到游戏记录容器元素");
            return;
        }
        
        // 加载记录
        fetch('/game/records', {
            cache: 'no-cache' // 确保每次都获取最新数据
        })
            .then(response => {
                if (!response.ok) {
                    log('error', `加载记录失败，服务器返回状态码: ${response.status}`);
                    return response.text().then(text => {
                        throw new Error(text || `加载记录失败，状态码: ${response.status}`);
                    });
                }
                
                // 检查响应内容类型
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    return response.json();
                } else {
                    throw new Error('服务器返回了非JSON格式的响应');
                }
            })
            .then(records => {
                const recordsList = document.getElementById('recordsList');
                if (!recordsList) {
                    log('error', "未找到记录列表元素");
                    return;
                }
                
                recordsList.innerHTML = '';
                
                if (!records || records.length === 0) {
                    log('info', "没有保存的棋局");
                    recordsList.innerHTML = '<p class="no-records">没有保存的棋局</p>';
                    return;
                }
                
                log('info', `加载到${records.length}条记录`);
                
                records.forEach((record, index) => {
                    try {
                        const date = new Date(record.saveTime);
                        // 不显示年份的日期格式 (MM/DD HH:MM)
                        const formattedDate = `${(date.getMonth()+1).toString().padStart(2, '0')}/${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
                        
                        const recordItem = document.createElement('div');
                        recordItem.className = 'record-item';
                        
                        const recordInfo = document.createElement('div');
                        recordInfo.className = 'record-info';
                        
                        // 添加保存时间信息
                        const timeInfo = document.createElement('div');
                        timeInfo.className = 'time-info';
                        timeInfo.textContent = `保存时间: ${formattedDate}`;
                        recordInfo.appendChild(timeInfo);
                        
                        // 添加用户信息（如果有）
                        if (record.username) {
                            const userInfo = document.createElement('div');
                            userInfo.className = 'user-info-record';
                            userInfo.textContent = `用户: ${record.username}`;
                            recordInfo.appendChild(userInfo);
                        }
                        
                        const buttonsDiv = document.createElement('div');
                        buttonsDiv.className = 'record-buttons';
                        
                        const loadButton = document.createElement('button');
                        loadButton.textContent = '加载';
                        loadButton.className = 'button';
                        loadButton.addEventListener('click', function() {
                            loadGame(record.id);
                        });
                        
                        const deleteButton = document.createElement('button');
                        deleteButton.textContent = '删除';
                        deleteButton.className = 'button';
                        deleteButton.addEventListener('click', function() {
                            deleteGame(record.id, recordItem);
                        });
                        
                        buttonsDiv.appendChild(loadButton);
                        buttonsDiv.appendChild(deleteButton);
                        
                        recordItem.appendChild(recordInfo);
                        recordItem.appendChild(buttonsDiv);
                        recordsList.appendChild(recordItem);
                    } catch (e) {
                        log('error', `处理记录时发生错误:`, e);
                    }
                });
            })
            .catch(error => {
                log('error', '加载记录失败:', error);
                console.error('加载记录失败: ' + error.message);
            });
    });
}

// 加载棋局
function loadGame(id) {
    safeExecute('loadGame', () => {
        log('info', `正在加载游戏记录，ID: ${id}`);
        fetch(`/game/load/${id}`)
            .then(response => {
                if (!response.ok) {
                    log('error', `加载记录失败，服务器返回状态码: ${response.status}`);
                    return response.text().then(text => {
                        if (response.status === 403) {
                            throw new Error(text || '没有权限加载此记录');
                        } else {
                            throw new Error(text || `加载记录失败，状态码: ${response.status}`);
                        }
                    });
                }
                
                // 检查响应内容类型
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    return response.json();
                } else {
                    throw new Error('服务器返回了非JSON格式的响应');
                }
            })
            .then(data => {
                log('info', "加载记录成功，数据:", data);
                
                // 解析棋盘状态
                try {
                    const parsedBoard = JSON.parse(data.boardState);
                    if (Array.isArray(parsedBoard)) {
                        // 更新全局变量
                        board = parsedBoard;
                        currentPlayer = data.currentPlayer;
                        gameOver = false;
                        
                        // 更新UI
                        updateBoardUI();
                        updateTurnIndicator();
                        
                        // 直接更新本地UI并显示成功消息
                        alert('加载成功！');
                        
                        // 重置游戏状态服务器端，确保可以继续游戏
                        fetch('/reset-state', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                boardState: data.boardState,
                                currentPlayer: data.currentPlayer
                            })
                        })
                        .then(response => {
                            if (!response.ok) {
                                // 忽略错误，已经在UI上显示成功了
                                log('warn', `重置服务器端游戏状态返回: ${response.status}`);
                                return null;
                            }
                            return response.json();
                        })
                        .then(resetData => {
                            if (resetData) {
                                log('info', '服务器端游戏状态已重置, 返回:', resetData);
                                // 确保本地状态与服务器一致
                                if (resetData.board) {
                                    board = resetData.board;
                                    updateBoardUI();
                                }
                                if (resetData.currentPlayer) {
                                    currentPlayer = resetData.currentPlayer;
                                    updateTurnIndicator();
                            }
                            }
                            
                            // 刷新记录列表
                            loadRecords();
                        })
                        .catch(error => {
                            // 忽略错误，已经在UI上成功加载了
                            log('error', '重置游戏状态错误，但游戏已加载:', error);
                        });
                    } else {
                        throw new Error('棋盘数据不是有效的数组');
                    }
                } catch (e) {
                    log('error', "解析棋盘数据失败:", e);
                    alert('解析棋盘数据失败: ' + e.message);
                }
            })
            .catch(error => {
                log('error', "加载失败:", error);
                alert(error.message);
            });
    });
}

// 删除游戏记录
function deleteGame(id, recordElement) {
    safeExecute('deleteGame', () => {
        if (confirm('确定要删除这条记录吗？')) {
            log('info', `删除游戏记录，ID: ${id}`);
            fetch(`/game/delete/${id}`, {
                method: 'DELETE'
            })
            .then(response => {
                if (!response.ok) {
                    log('error', `删除记录失败，服务器返回状态码: ${response.status}`);
                    return response.text().then(text => {
                        if (response.status === 403) {
                            throw new Error(text || '没有权限删除此记录');
                        } else {
                            throw new Error(text || `删除记录失败，状态码: ${response.status}`);
                        }
                    });
                }
                recordElement.remove();
                alert('删除成功！');
            })
            .catch(error => {
                log('error', "删除失败:", error);
                alert(error.message);
            });
        }
    });
}

// 保存棋局
function saveGame() {
    safeExecute('saveGame', () => {
        log('info', "正在保存棋局");
        
        if (!board || !Array.isArray(board)) {
            log('error', "无法保存，棋盘数据无效");
            alert("无法保存，棋盘数据无效");
            return;
        }
        
        const gameData = {
            boardState: JSON.stringify(board),
            currentPlayer: currentPlayer,
            saveTime: new Date().getTime()
        };
    
        log('info', "发送保存请求，数据:", gameData);
        
        fetch('/game/save', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(gameData)
        })
        .then(response => {
            if (!response.ok) {
                log('error', `保存失败，服务器返回状态码: ${response.status}`);
                return response.text().then(text => {
                    throw new Error(text || `保存失败，状态码: ${response.status}`);
                });
            }
            
            // 检查响应内容类型
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return response.json();
            } else {
                // 如果不是JSON，按文本处理
                return response.text().then(text => {
                    return { message: text };
                });
            }
        })
        .then(data => {
            log('info', '保存成功，返回数据:', data);
            alert('保存成功！');
            
            // 刷新记录列表
            loadRecords();
        })
        .catch(error => {
            log('error', '保存失败:', error);
            alert('保存失败: ' + error.message);
        });
    });
}

// 事件监听器绑定
function bindEventListeners() {
    safeExecute('bindEventListeners', () => {
        // 重置按钮
        const resetButton = document.getElementById('resetButton');
        if (resetButton) {
            resetButton.addEventListener('click', resetGame);
            log('info', "绑定重置按钮事件");
        }
        
        // 模式切换按钮
        const toggleModeButton = document.getElementById('toggleModeButton');
        if (toggleModeButton) {
            toggleModeButton.addEventListener('click', toggleGameMode);
            log('info', "绑定模式切换按钮事件");
        }
        
        // 保存按钮
        const saveButton = document.getElementById('saveButton');
        if (saveButton) {
            saveButton.addEventListener('click', saveGame);
            log('info', "绑定保存按钮事件");
        }
        
        // 绑定棋盘单元格点击事件
        const cells = document.querySelectorAll('.cell');
        cells.forEach(cell => {
            cell.addEventListener('click', function() {
                const x = parseInt(this.dataset.x);
                const y = parseInt(this.dataset.y);
                
                if (isAIMode && currentPlayer === 'white') {
                    // AI回合，玩家不能落子
                    return;
                }
                
                if (gameOver) {
                    alert('游戏已结束，请重置棋盘！');
                    return;
                }
                
                if (board[y][x] === null) {
                    fetch('/move', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({x, y, player: currentPlayer})
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.valid) {
                            board[y][x] = currentPlayer;
                            updateBoardUI();
                            
                            if (checkWin(x, y, currentPlayer)) {
                                alert(currentPlayer === 'black' ? '黑棋胜利！' : '白棋胜利！');
                                gameOver = true;
                            } else {
                                currentPlayer = currentPlayer === 'black' ? 'white' : 'black';
                                updateTurnIndicator();
                                
                                // 如果是AI模式且轮到AI(白棋)
                                if (isAIMode && currentPlayer === 'white' && !gameOver) {
                                    // 稍微延迟一下AI的落子，让玩家能看清楚
                                    setTimeout(() => {
                                        aiMove();
                                    }, 500);
                                }
                            }
                        }
                    })
                    .catch(error => {
                        log('error', '移动错误:', error);
                    });
                }
            });
        });
    });
}

// 页面加载时初始化
function initialize() {
    safeExecute('initialize', () => {
        log('info', "初始化游戏...");
        
        // 绘制初始棋盘
        drawBoard();
        
        // 加载已保存的棋局记录
        loadRecords();
        
        // 绑定事件监听器
        bindEventListeners();
        
        // 设置初始回合指示
        updateTurnIndicator();
        
        log('info', "游戏初始化完成");
    });
}

// 当页面加载完成时，初始化游戏
document.addEventListener('DOMContentLoaded', initialize);