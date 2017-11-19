# CodeChat

## Casual Coding on Android

Followup to flowgrid.org (drawing seems a bit too cumbersome but everybody is familiar with WhatsApp...) 

  * First demo: https://www.youtube.com/watch?v=zY4TIW3jDM0
  * Todo: https://docs.google.com/document/d/1eGk_CbD5Kd9OSJywEAKCGGfS6Fmp5JMu0_t6gP1nFKA/edit?usp=sharing

## Examples

### Bouncing Ball

    ball = new Sprite;
    ball.size = 25;
    ball.dx = -1;
    ball.xAlign = LEFT;
    ball.yAlign = BOTTOM;
    on ball.x > screen.width - ball.size:
      ball.dx = -10.0;
      ball.rotation = -90.0;
    end;
    on ball.y < 0.0:
      ball.dy = 100.0;
      ball.face = "😲";
      wait 0.2;
      ball.face = "😄";
    end;
    on ball.x < 0.0:
      ball.dx = 10.0;
      ball.rotation = 90.0;
    end;
    onchange screen.frame:
      ball.dy = ball.dy - 1.5;
    end;

### Rotating Spaceship

    ship = new Sprite;
    ship.face = "🚀";
    ship.size = 25;

    leftButton = new Sprite;
    leftButton.face = "↪️";
    leftButton.size = 25;
    leftButton.xAlign = LEFT;
    leftButton.yAlign = BOTTOM;

    rightButton = new Sprite;
    rightButton.face = "↩️";
    rightButton.size = 25;
    rightButton.x = 25;
    rightButton.xAlign = LEFT;
    rightButton.yAlign = BOTTOM;

    on leftButton.touch:
      ship.rotation = -90;
    end;
    on rightButton.touch:
      ship.rotation = 90;
    end;
    on not rightButton.touch and not leftButton.touch:
      ship.rotation = 0;
    end;
    
#### Shooting

    function shoot(): Void :
      var shot = new Sprite;
      shot.face = "⭐";
      shot.speed = 100;
      shot.direction = ship.angle - 45.0;
      play "🔫";
    end;
    
    fireButton = new Sprite;
    fireButton.face = "⏺️";
    fireButton.size = 25;
    fireButton.xAlign = RIGHT;
    fireButton.yAlign = BOTTOM;
    
    on fireButton.touch:
     shoot;
    end;
    
#### Adding asteroids

    proc addAsteroid():
      let a = new Sprite;
      a.face = "🥔";
      a.edgeMode = WRAP;     
      a.xAlign = LEFT;
      a.dx = 5 - (random()) × 10; 
      a.dy = 5 - (random()) × 10;
      a.yAlign = TOP;
      a.size = 30;
      a.rotation = 10 - 20 × (random());
    end;
