package com.freza.openworld;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.MotionEvent;
import android.graphics.*;
import java.util.*;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setContentView(new GameView());
    }

    static class GameView extends View {
        Paint p = new Paint(3);
        float px=0, py=0, carX=170, carY=80, angle=0;
        boolean up,down,left,right,fire, driving=false;
        long last=System.nanoTime();
        ArrayList<float[]> shots = new ArrayList<>();
        String message="FREZA CITY  •  استكشف المدينة";
        float scale=1, cx, cy;
        final int grass=Color.rgb(48,105,61), road=Color.rgb(54,58,64);
        GameView() { super(MainActivity.this); setLayerType(View.LAYER_TYPE_SOFTWARE,null); }
        void paint(Canvas c,int color) { p.setColor(color); p.setStyle(Paint.Style.FILL); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w=getWidth(), h=getHeight(); cx=w/2; cy=h/2;
            float dt=Math.min(.04f,(System.nanoTime()-last)/1e9f); last=System.nanoTime();
            float speed=driving?240:130;
            float dx=(right?1:0)-(left?1:0), dy=(down?1:0)-(up?1:0);
            if(dx!=0||dy!=0) { float n=(float)Math.sqrt(dx*dx+dy*dy); dx/=n;dy/=n; }
            if(driving) { carX+=dx*speed*dt; carY+=dy*speed*dt; px=carX;py=carY; }
            else { px+=dx*speed*dt;py+=dy*speed*dt; }
            canvas.drawColor(grass);
            canvas.save(); canvas.translate(cx-px,cy-py);
            // City blocks and road grid
            paint(canvas,road);
            for(int i=-8;i<=8;i++){
                canvas.drawRect(i*260-45,-2400,i*260+45,2400,p);
                canvas.drawRect(-2400,i*260-45,2400,i*260+45,p);
            }
            paint(canvas,Color.rgb(225,205,130));
            for(int i=-8;i<=8;i++){
                for(int j=-8;j<=8;j++){
                    float x=i*260,y=j*260;
                    if(Math.abs(x-px)<1250 && Math.abs(y-py)<1250){
                        paint(canvas,Color.rgb(30,34,39));
                        canvas.drawRoundRect(x+65,y+65,x+205,y+205,9,9,p);
                        paint(canvas,new int[]{Color.rgb(172,119,78),Color.rgb(104,130,151),Color.rgb(180,164,119)}[(i-j+30)%3]);
                        canvas.drawRect(x+75,y+75,x+195,y+195,p);
                        paint(canvas,Color.rgb(50,55,60));
                        canvas.drawRect(x+75,y+75,x+195,y+88,p);
                    }
                }
            }
            // parked car
            drawCar(canvas,carX,carY,driving?angle:0,Color.rgb(30,110,220));
            // pedestrians
            for(int i=0;i<12;i++){
                float x=(float)(Math.sin(i*13.4)*650), y=(float)(Math.cos(i*9.1)*650);
                paint(canvas,Color.rgb(235,190,145)); canvas.drawCircle(x,y,9,p);
                paint(canvas,Color.rgb(190,50+(i*13)%130,65)); canvas.drawRoundRect(x-7,y+8,x+7,y+30,4,4,p);
            }
            // projectiles
            paint(canvas,Color.YELLOW);
            for(int i=shots.size()-1;i>=0;i--){ float[] s=shots.get(i);s[0]+=s[2]*dt;s[1]+=s[3]*dt;s[4]-=dt;canvas.drawCircle(s[0],s[1],5,p);if(s[4]<=0)shots.remove(i); }
            // player
            if(!driving){
                paint(canvas,Color.rgb(250,190,55)); canvas.drawCircle(px,py,13,p);
                paint(canvas,Color.rgb(40,45,55)); canvas.drawCircle(px,py-4,7,p);
            }
            canvas.restore();
            drawHud(canvas,w,h);
            postInvalidateDelayed(16);
        }
        void drawCar(Canvas c,float x,float y,float a,int color){
            c.save();c.rotate((float)Math.toDegrees(a),x,y);
            paint(c,Color.BLACK);c.drawRoundRect(x-27,y-43,x+27,y+43,12,12,p);
            paint(c,color);c.drawRoundRect(x-23,y-40,x+23,y+40,10,10,p);
            paint(c,Color.rgb(135,190,210));c.drawRect(x-17,y-25,x+17,y-7,p);c.drawRect(x-17,y+10,x+17,y+28,p);
            paint(c,Color.YELLOW);c.drawRect(x-19,y-42,x-8,y-37,p);c.drawRect(x+8,y-42,x+19,y-37,p);
            c.restore();
        }
        void round(Canvas c,float x,float y,float r,int color){paint(c,color);c.drawRoundRect(x,y,x+r,y+r,14,14,p);}
        void drawHud(Canvas c,float w,float h){
            paint(c,0xCC101820);c.drawRoundRect(14,12,Math.min(w-14,410),58,14,14,p);
            p.setColor(Color.WHITE);p.setTextSize(22);c.drawText(message,28,42,p);
            // left directional pad
            int a=0xBB202B36; round(c,24,h-150,58,a);round(c,86,h-88,58,a);round(c,86,h-212,58,a);round(c,148,h-150,58,a);
            round(c,w-150,h-150,62,0xCCB02B24);round(c,w-150,h-225,62,0xCC1D6D9C);
            p.setColor(Color.WHITE);p.setTextSize(24);
            c.drawText("◀",39,h-113,p);c.drawText("▶",164,h-113,p);c.drawText("▲",101,h-174,p);c.drawText("▼",101,h-50,p);
            c.drawText("FIRE",w-139,h-113,p);c.drawText(driving?"EXIT":"CAR",w-139,h-188,p);
            p.setTextSize(13);c.drawText("المشي • السيارة • إطلاق تجريبي",w/2-95,h-18,p);
        }
        @Override public boolean onTouchEvent(MotionEvent e){
            int act=e.getActionMasked(), idx=e.getActionIndex(); float x=e.getX(idx),y=e.getY(idx),w=getWidth(),h=getHeight();
            if(act==MotionEvent.ACTION_DOWN||act==MotionEvent.ACTION_POINTER_DOWN){
                if(x<220 && y>h-240){ if(x<82){left=true;right=false;} else if(x>145){right=true;left=false;} else {up=y<h-150;down=!up;} }
                else if(x>w-180 && y>h-245){
                    if(y>h-165){fire=true;float d=driving?angle:0;shots.add(new float[]{px,py-25,(float)Math.sin(d)*520,(float)-Math.cos(d)*520,1.2f});}
                    else {driving=!driving;message=driving?"داخل السيارة — قد السيارة":"خرجت من السيارة";}
                }
                return true;
            }
            if(act==MotionEvent.ACTION_UP||act==MotionEvent.ACTION_POINTER_UP||act==MotionEvent.ACTION_CANCEL){
                left=right=up=down=fire=false;return true;
            }
            return true;
        }
    }
}
