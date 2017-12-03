# CodeChat

## Casual Coding on Android

Followup to flowgrid.org (drawing seems a bit too cumbersome but everybody is familiar with WhatsApp...) 

  * First demo: https://www.youtube.com/watch?v=zY4TIW3jDM0
  * Todo: https://docs.google.com/document/d/1eGk_CbD5Kd9OSJywEAKCGGfS6Fmp5JMu0_t6gP1nFKA/edit?usp=sharing

## Examples

### Bouncing Ball

    ball = new Sprite
    ball.size = 25
    ball.dx = -1
    ball.xAlign = LEFT
    ball.yAlign = BOTTOM
    on ball.x > screen.width - ball.size:
      ball.dx = -10.0
      ball.rotation = -90.0
    end
    on ball.y < 0.0:
      ball.dy = 100.0
      ball.face = "😲"
      wait 0.2
      ball.face = "😄"
    end
    on ball.x < 0.0:
      ball.dx = 10.0
      ball.rotation = 90.0
    end
    onchange screen.frame:
      ball.dy = ball.dy - 1.5
    end

### More

https://github.com/stefanhaustein/codechat/tree/master/examples
