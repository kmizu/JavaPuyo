package com.github.kmizu.java_game.puyo;

import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Event;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.imageio.*;
import java.io.File;
public class JavaPuyo extends JPanel {
        PuyoArray2D field;
        PuyoJudge judge;
        PuyoMover mover;
        KeyAdapter adapter;
        MouseAdapter mAdapter;
        Image offscrn;
        Graphics offg;
        Puyo puyo;
        int rensa;
        int maxRensa;
        boolean firstClick;
        public JavaPuyo() {
                setPreferredSize(new Dimension(800, 600));
                setFocusable(true);
                requestFocusInWindow();
                firstClick=true;
                try {
                    PuyoColor.initClass(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                field=new PuyoArray2D(8,16);
                judge=new PuyoJudge(field);
                mover=new PuyoMover(field);
                mAdapter=new MouseAdapter(){
                        public void mousePressed(MouseEvent evt){
                                if(firstClick){
                                        removeMouseListener(this);
                                        firstClick=false;
                                        new JThread().start();
                                        try{
                                                Thread.sleep(200);
                                        }catch(InterruptedException e){}
                                        new GThread().start();
                                }
                        }
                };
                adapter=new KeyAdapter(){
                        public void keyPressed(KeyEvent evt){
                                synchronized(mover){
                                        switch(evt.getKeyCode()){
                                                case KeyEvent.VK_SPACE:
                                                        mover.try_turn();
                                                        break;
                                                case KeyEvent.VK_DOWN:
                                                        if(!mover.try_down()){
                                                                mover.notifyAll();
                                                                removeKeyListener(adapter);
                                                        }
                                                        break;
                                                case KeyEvent.VK_LEFT:
                                                        mover.try_left();
                                                        break;
                                                case KeyEvent.VK_RIGHT:
                                                        mover.try_right();
                                                        break;
                                        }
                                        repaint();
                                }
                        }
                };
                addKeyListener(adapter);
                addMouseListener(mAdapter);
                mover.ready();
                mover.ready();
        }
        public void paintComponent(Graphics g){
                if(offscrn == null) {
                    offscrn = createImage(240,450);
                    offg = offscrn.getGraphics();
                }
                field.draw(offg,0,0);
                mover.draw(offg,0,0);
                offg.setColor(Color.white);
                offg.drawString("最高" + maxRensa +"連鎖",20,60);
                g.drawImage(offscrn,0,0,this);
        }
        public void update(Graphics g){paint(g);}
        public void stop(){}
        class GThread extends Thread{
                public void run(){
                        while(true){
                                synchronized(mover){
                                        if(!mover.try_down()){
                                                mover.notifyAll();
                                                removeKeyListener(adapter);
                                                try{
                                                        Thread.sleep(100);
                                                }catch(InterruptedException e){}
                                        }
                                        repaint();
                                }
                                try{
                                        Thread.sleep(600);
                                }catch(InterruptedException e){}
                        }
                }
        }
        class JThread extends Thread{
                public void run(){
                        synchronized(mover){
                                while(true){
                                        try{
                                                mover.wait();

                                        }catch(InterruptedException e){}
                                        try{
                                                Thread.sleep(100);
                                        }catch(InterruptedException e){}
                                        repaint();
                                        while(judge.allJudge()){
                                                rensa++;
                                                repaint();
                                                try{
                                                        Thread.sleep(300);
                                                }catch(InterruptedException e){}
                                                judge.shiftAll();
                                                repaint();
                                                try{
                                                        Thread.sleep(300);
                                                }catch(InterruptedException e){}
                                        }
                                        if(maxRensa<rensa){
                                                maxRensa=rensa;
                                        }
                                        rensa=0;
                                        addKeyListener(adapter);
                                }
                        }
                }
        }
        public static void main(String[] args) {
                var frame = new JFrame("Java Puyo");
                var panel = new JavaPuyo();
                frame.getContentPane().add(panel);
                frame.pack();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setResizable(false);
                frame.setVisible(true);
                panel.setBackground(Color.GREEN);
                frame.validate();
                panel.validate();
        }
}
class Puyo{
        public static final int WIDTH=30;
        public static final int HEIGHT=28;
        public static Puyo randomPuyo(int kind){
                return new Puyo(PuyoColor.randPuyoColor(kind));
        }
        private PuyoColor color;
        public Puyo(PuyoColor color){
                this.color=color;
        }
        public boolean equalColor(Puyo puyo){
                return(getColor()==puyo.getColor());
        }
        public PuyoColor getColor(){
                return color;
        }
        public String toString(){
                return color.toString();
        }
        public void draw(Graphics g,int x,int y){
                g.drawImage(color.getColor(),x,y,WIDTH,HEIGHT,null);
        }
}
class MovablePuyo{
        public static MovablePuyo[] newPuyo(int kind){
                MovablePuyo[] array=new MovablePuyo[2];
                array[0]=new MovablePuyo(Puyo.randomPuyo(kind),3,-1);
                array[1]=new MovablePuyo(Puyo.randomPuyo(kind),3,-2);
                return array;
        }
        private Puyo puyo;
        private int x;
        private int y;
        MovablePuyo(Puyo puyo,int x,int y){
                this.puyo=puyo;
                this.x=x;
                this.y=y;
        }
        public void draw(Graphics g,int x,int y){
                Image img=getColor();
                int X=x+this.x*Puyo.WIDTH;
                int Y=y+this.y*Puyo.HEIGHT;
                g.drawImage(img,X,Y,Puyo.WIDTH,Puyo.HEIGHT,null);
        }
        public Puyo getPuyo(){
                return puyo;
        }
        public Image getColor(){
                return puyo.getColor().getColor();
        }
        public int getX(){
                return x;
        }
        public void setX(int x){
                this.x=x;
        }
        public int getY(){
                return y;
        }
        public void setY(int y){
                this.y=y;
        }
        public void left(){x--;}
        public void right(){x++;}
        public void up(){y--;}
        public void down(){y++;}
}
final class PuyoColor{
        private static PuyoColor PURPLE;
        private static PuyoColor RED;
        private static PuyoColor YELLOW;
        private static PuyoColor BLUE;
        private static PuyoColor ORANGE;
        private static PuyoColor GRAY;
        private static PuyoColor BLACK;
        private static int num=6;
        public static void initClass(Component app) throws Exception {
                MediaTracker mt=new MediaTracker(app);
                COLOR=new PuyoColor[num];
                COLOR[0]=PURPLE=new PuyoColor(ImageIO.read(new File("purple_puyo.jpg")));
                COLOR[1]=RED=new PuyoColor(ImageIO.read(new File("red_puyo.jpg")));
                COLOR[2]=YELLOW=new PuyoColor(ImageIO.read(new File("yellow_puyo.jpg")));
                COLOR[3]=BLUE=new PuyoColor(ImageIO.read(new File("blue_puyo.jpg")));
                COLOR[4]=ORANGE=new PuyoColor(ImageIO.read(new File("orange_puyo.jpg")));
                COLOR[5]=GRAY=new PuyoColor(ImageIO.read(new File("gray_puyo.jpg")));
                BLACK=new PuyoColor(ImageIO.read(new File("black_puyo.jpg")));
                for(int i=0;i<COLOR.length;i++){
                        mt.addImage(COLOR[i].getColor(),i);
                }
                try{
                        mt.waitForAll();
                }catch(InterruptedException e){}
        }
        public static PuyoColor randPuyoColor(int kind){
                int length=0;
                int index=0;
                if(kind<=0||kind>COLOR.length){
                        length=COLOR.length;
                }else{
                        length=kind;
                }
                index=(int)(Math.random()*length);
                return COLOR[index];
        }
        private static PuyoColor[] COLOR;
        private final Image color;
        private PuyoColor(Image color){
                this.color=color;
        }
        public Image getColor(){return color;}
        public String toString(){return "not define";}
}
class PuyoArray2D{
        private static int PAT=2;
        private int dWidth;
        private int dHeight;
        private int rWidth;
        private int rHeight;
        private Puyo[][] puyo;
        public PuyoArray2D(int rWidth,int rHeight){
                this.rWidth=rWidth;
                this.rHeight=rHeight;
                this.dWidth=rWidth*Puyo.WIDTH;
                this.dHeight=rHeight*Puyo.HEIGHT;
                puyo=new Puyo[rWidth][rHeight+PAT];
        }
        public void put(Puyo aPuyo,int x,int y){
                puyo[x][y+PAT]=aPuyo;
        }
        public Puyo get(int x,int y){
                return puyo[x][y+PAT];
        }
        public boolean shift(int x,int y){
                if(x>=0&&x<rWidth){
                        if(y>=-2&&y+1<rHeight){
                                if(get(x,y+1)==null){
                                        put(get(x,y),x,y+1);
                                        delete(x,y);
                                        return true;
                                }
                        }
                }
                return false;
        }
        public void draw(Graphics g,int x,int y){
                g.setColor(Color.black);
                g.fillRect(x,y,dWidth,dHeight);
                for(int X=0;X<rWidth;X++){
                        for(int Y=0;Y<rHeight;Y++){
                                if(get(X,Y)!=null){
                                        get(X,Y).draw(g,x+X*Puyo.WIDTH,y+Y*Puyo.HEIGHT);
                                }
                        }
                }
        }
        public Puyo delete(int x,int y){
                Puyo temp=null;
                temp=puyo[x][y+PAT];
                puyo[x][y+PAT]=null;
                return temp;
        }
        public int getRWidth(){return rWidth;}
        public int getRHeight(){return rHeight;}
        public boolean delete(Puyo puyo){
                for(int x=0;x<rWidth;x++){
                        for(int y=-2;y<rHeight;y++){
                                if(this.puyo[x][y+PAT]==puyo){
                                        this.puyo[x][y+PAT]=null;
                                }
                        }
                }
                return false;
        }
}
class PuyoMover{
        private PuyoArray2D field;
        private MovablePuyo[] now;
        private MovablePuyo[] next;
        private boolean locked=false;
        public PuyoMover(PuyoArray2D field){
                this.field=field;
        }
        public void ready(){
                now=next;
                next=MovablePuyo.newPuyo(5);
        }
        public synchronized boolean try_right(){
                now[0].right();
                now[1].right();
                for(int i=0;i<now.length;i++){
                        if(now[i].getX()>=field.getRWidth()
                                        ||field.get(now[i].getX(),now[i].getY())!=null){
                                now[0].left();
                                now[1].left();
                                return false;
                                        }
                }
                return true;
        }
        public synchronized boolean try_turn(){
                int x0=now[0].getX();
                int x1=now[1].getX();
                int y0=now[0].getY();
                int y1=now[1].getY();
                boolean turned=true;
                if(x0==x1){
                        if(y0<y1){
                                now[1].right();
                                now[1].up();
                        }
                        else{
                                now[1].left();
                                now[1].down();
                        }
                }
                else if(y0==y1){
                        if(x0<x1){
                                now[1].left();
                                now[1].up();
                        }
                        else{
                                now[1].right();
                                now[1].down();
                        }
                }
                if(now[1].getX()<0
                                ||now[1].getX()>=field.getRWidth()
                                ||now[1].getY()<-2
                                ||now[1].getY()>=field.getRHeight()
                                ||field.get(now[1].getX(),now[1].getY())!=null){
                        now[1].setX(x1);
                        now[1].setY(y1);
                        turned=false;
                                }
                return turned;
        }
        public synchronized boolean try_left(){
                now[0].left();
                now[1].left();
                for(int i=0;i<now.length;i++){
                        if(now[i].getX()<0
                                        ||field.get(now[i].getX(),now[i].getY())!=null){
                                now[0].right();
                                now[1].right();
                                return false;
                                        }
                }
                return true;
        }
        public synchronized boolean try_down(){
                int x;
                int y;
                now[0].down();
                now[1].down();
                for(int i=0;i<now.length;i++){
                        if(now[i].getY()>=field.getRHeight()
                                        ||field.get(now[i].getX(),now[i].getY())!=null){
                                now[0].up();
                                now[1].up();
                                copy();
                                if(i==0){
                                        x=now[1].getX();
                                        y=now[1].getY();
                                }else{
                                        x=now[0].getX();
                                        y=now[0].getY();
                                }
                                while(field.shift(x,y)){
                                        y++;
                                }
                                ready();
                                return false;
                                        }
                }
                return true;
        }
        public void draw(Graphics g,int x,int y){
                now[0].draw(g,x+0,y+0);
                now[1].draw(g,x+0,y+0);
        }
        private synchronized void copy(){
                int x;
                int y;
                for(int i=0;i<now.length;i++){
                        x=now[i].getX();
                        y=now[i].getY();
                        field.put(now[i].getPuyo(),x,y);
                }
        }
}
class PuyoJudge{
        private PuyoArray2D field;
        private Vector alreadySearched=new Vector(50);
        private Vector sameColor=new Vector(50);
        private Enumeration iterator;
        private Puyo first;
        private boolean same(Puyo puyo){
                iterator=alreadySearched.elements();
                Object compare;
                while(iterator.hasMoreElements()){
                        compare=iterator.nextElement();
                        if(((Puyo)compare)==puyo){
                                return true;
                        }
                }
                return false;
        }
        public PuyoJudge(PuyoArray2D field){
                this.field=field;
        }
        public void shiftAll(){
                for(int x=0;x<field.getRWidth();x++){
                        shiftEmptyLine(x);
                }
        }
        private void shiftEmptyLine(int x){
                for(int y=field.getRHeight()-1;y>=-2;y--){
                        shiftEmptyBlock(x,y);
                }
        }
        private void shiftEmptyBlock(int x,int y){
                while(field.shift(x,y)==true){
                        y++;
                }
        }
        public boolean allJudge(){
                boolean deleted=false;
                for(int x=0;x<field.getRWidth();x++){
                        for(int y=-2;y<field.getRHeight();y++){
                                if(judge(x,y)==true){
                                        deleted=true;
                                }
                        }
                }
                return deleted;
        }
        private boolean judge(int x,int y){
                boolean deleted=false;
                first=field.get(x,y);
                search(x,y);
                if(sameColor.size()>=4){
                        deleted=true;
                        iterator=sameColor.elements();
                        while(iterator.hasMoreElements()){
                                field.delete(((Puyo)iterator.nextElement()));
                        }
                }
                alreadySearched.removeAllElements();
                sameColor.removeAllElements();
                return deleted;
        }
        private void search(int x,int y){
                if(x<0||x>=field.getRWidth()||y<-2||y>=field.getRHeight()){
                        return;
                }
                Puyo nowPuyo=field.get(x,y);
                if(nowPuyo==null){
                        return;
                }
                if(same(nowPuyo)){
                        return;
                }
                alreadySearched.addElement(nowPuyo);
                if(first.equalColor(nowPuyo)){
                        sameColor.addElement(nowPuyo);
                }else{
                        return;
                }
                search(x+1,y);
                search(x,y+1);
                search(x-1,y);
                search(x,y-1);
        }

}
