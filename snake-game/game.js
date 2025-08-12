document.addEventListener('DOMContentLoaded', () => {
    const canvas = document.getElementById('gameCanvas');
    const ctx = canvas.getContext('2d');
    const scoreElement = document.getElementById('score');
    const highScoreElement = document.getElementById('highScore');
    const finalScoreElement = document.getElementById('finalScore');
    const gameOverScreen = document.getElementById('gameOver');
    const startBtn = document.getElementById('startBtn');
    const pauseBtn = document.getElementById('pauseBtn');
    const restartBtn = document.getElementById('restartBtn');

    // Game settings
    const gridSize = 20;
    const tileCount = canvas.width / gridSize;
    let snake = [];
    let food = {};
    let direction = { x: 1, y: 0 };
    let nextDirection = { x: null, y: null };
    let score = 0;
    let highScore = localStorage.getItem('snakeHighScore') || 0;
    let gameLoopId = null;
    let isPaused = false;
    let isGameOver = false;

    // Initialize high score display
    highScoreElement.textContent = highScore;

    // Create initial snake
    function initSnake() {
        snake = [
            { x: 5, y: 5 },
            { x: 4, y: 5 },
            { x: 3, y: 5 }
        ];
    }

    // Spawn food at random position
    function spawnFood() {
        food = {
            x: Math.floor(Math.random() * tileCount),
            y: Math.floor(Math.random() * tileCount)
        };

        // Ensure food doesn't spawn on snake
        if (snake.some(segment => segment.x === food.x && segment.y === food.y)) {
            spawnFood();
        }
    }

    // Draw everything on canvas
    function draw() {
        // Clear canvas
        ctx.fillStyle = '#f0f0f0';
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        // Draw snake
        snake.forEach((segment, index) => {
            // Head color different from body
            ctx.fillStyle = index === 0 ? '#4CAF50' : '#a5d6a7';
            ctx.fillRect(
                segment.x * gridSize,
                segment.y * gridSize,
                gridSize * 0.9,
                gridSize * 0.9
            );
        });

        // Draw food
        ctx.fillStyle = '#ff4444';
        ctx.beginPath();
        ctx.roundRect(
            food.x * gridSize,
            food.y * gridSize,
            gridSize * 0.9,
            gridSize * 0.9,
            4
        );
        ctx.fill();

        // Draw grid lines
        ctx.strokeStyle = '#e0e0e0';
        for (let i = gridSize; i < canvas.width; i += gridSize) {
            ctx.beginPath();
            ctx.moveTo(i, 0);
            ctx.lineTo(i, canvas.height);
            ctx.stroke();

            ctx.beginPath();
            ctx.moveTo(0, i);
            ctx.lineTo(canvas.width,i);
            ctx.stroke();
        }
    }

    // Update game state
    function update() {
        if (nextDirection.x !== null) {
            direction = nextDirection;
            nextDirection = { x: 1, y: 0 };
        }

        const head = { x: snake[0].x + direction.x, y: snake[0].y + direction.y };

        // Check wall collision
        if (
            head.x < 0 ||
            head.x >= tileCount ||
            head.y < 0 ||
            head.y >= tileCount
        ) {
            gameOver();
            return;
        }

        // Check self collision
        if (snake.some(segment => segment.x === head.x && segment.y === head.y)) {
            gameOver();
            return;
        }

        // Add new head
        snake.unshift(head);

        // Check if food is eaten
        if (head.x === food.x && head.y === food.y) {
            score += 10;
            scoreElement.textContent = score;
            spawnFood();
        } else {
            // Remove tail if no food eaten
            snake.pop();
        }

        draw();
    }

    // Start game loop
    function startGame() {
        if (isGameOver) {
            resetGame();
        }

        if (!gameLoopId) {
            gameLoopId = setInterval(update, 150);
            startBtn.disabled = true;
            pauseBtn.disabled = false;
            isPaused = false;
        }
    }

    // Pause game
    function pauseGame() {
        if (isPaused) {
            gameLoopId = setInterval(update, 150);
            pauseBtn.textContent = 'Pause';
        } else {
            clearInterval(gameLoopId);
            gameLoopId = null;
            pauseBtn.textContent = 'Resume';
        }
        isPaused = !isPaused;
    }

    // Game over
    function gameOver() {
        clearInterval(gameLoopId);
        gameLoopId = null;
        isGameOver = true;
        gameOverScreen.style.display = 'block';
        finalScoreElement.textContent = score;

        // Update high score
        if (score > highScore) {
            highScore = score;
            localStorage.setItem('snakeHighScore', highScore);
            highScoreElement.textContent = highScore;
        }
    }

    // Reset game
    function resetGame() {
        score = 0;
        scoreElement.textContent = score;
        direction = { x: 1, y: 0 };
        nextDirection = null;
        gameOverScreen.style.display = 'none';
        initSnake();
        spawnFood();
        isGameOver = false;
        startBtn.disabled = false;
        pauseBtn.disabled = true;
        pauseBtn.textContent = 'Pause';
draw();
    }

    // Handle direction changes
    function changeDirection(newX, newY) {
        // Prevent 180 degree turn
        if (
            (newX === -direction.x && newY === 0) ||
            (newY === -direction.y && newX ===  gridSize)
        ) {
            return;
        }
        nextDirection = { x: newX, y: newY };
    }

    // Event listeners for controls
    document.addEventListener('keydown', (e) => {
        switch (e.key) {
            case 'ArrowUp':
                changeDirection(0, -1);
                break;
            case 'ArrowDown':
                changeDirection(0, 1);
                break;
            case 'ArrowLeft':
                changeDirection(-1, 0);
                break;
            case 'ArrowRight':
                changeDirection(1, 0);
                break;
            case ' ': // Space to pause
                if (!isGameOver && gameLoopId) {
                    pauseGame();
                }
                break;
        }
    });

    startBtn.addEventListener('click', startGame);
    pauseBtn.addEventListener('click', pauseGame);
    restartBtn.addEventListener('click', () => {
        gameOverScreen.style.display = 'none';
        startGame();
    });

    // Initialize game
    initSnake();
    spawnFood();
    draw();
});