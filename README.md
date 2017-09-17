# CodeChat

## Casual Coding on Android

Followup to flowgrid.org (drawing seems a bit too cumbersome but everybody is familiar with WhatsApp...) 

  * First demo: https://www.youtube.com/watch?v=zY4TIW3jDM0
  * Todo: https://docs.google.com/document/d/1eGk_CbD5Kd9OSJywEAKCGGfS6Fmp5JMu0_t6gP1nFKA/edit?usp=sharing

## Examples

### Bouncing Ball

    ball = new Sprite
    ball.xAlign = LEFT;
    ball.yAlign = BOTTOM;
    on ball.x > screen.width - ball.size:
      ball.dx = -100.0;
      ball.rotation = 3.0;
    end;
    on ball.y < 0.0:
      ball.dy = 1000.0;
      ball.face = "😲";
      wait 0.2;
      ball.face = "😄";
    end;
    on ball.x < 0.0:
      ball.dx = 100.0;
      ball.rotation = -3.0;
    end;
    onchange screen.frame:
      ball.dy = ball.dy - 15.0;
    end;

### Rotating Spaceship

    ship = new Sprite;
    ship.face = "🚀";
    ship.size = 250.0;

    leftButton = new Sprite;
    leftButton.face = "↪️";
    leftButton.size = 250;
    leftButton.xAlign = LEFT;
    leftButton.yAlign = BOTTOM;

    rightButton = new Sprite;
    rightButton.face = "↩️";
    rightButton.size = 250.0;
    rightButton.x = 250.0;
    rightButton.xAlign = LEFT;
    rightButton.yAlign = BOTTOM;

    on leftButton.touch:
      ship.rotation = 3.0;
    end;
    on rightButton.touch:
      ship.rotation = -3.0;
    end;
    on not rightButton.touch and not leftButton.touch:
      ship.rotation = 0.0;
    end;
    
#### Shooting

    function shoot(): Void :
      var shot = new Sprite;
      shot.face = "⭐";
      shot.speed = 1000.0;
      shot.direction = ship.angle + 45.0°;
      on ¬shot.visible:
        delete shot;
      end;
    end;
    
    fireButton = new Sprite;
    fireButton.face = "⏺️";
    fireButton.size = 250.0;
    fireButton.xlAlign = RIGHT;
    fireButton.yAlign = BOTTOM;
    
    on fireButton.touched:
     shoot;
    end;

 
