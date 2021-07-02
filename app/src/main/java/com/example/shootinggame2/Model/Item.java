package com.example.shootinggame2.Model;

public abstract class Item {

    protected final int id; //Game에서 관리하기 위해 할당되는 id

    protected float dx; // 이동하는 단위 벡터
    protected float dy;

    protected float x; // 위치 좌표
    protected float y;

    public Item(int id) {
        this.id = id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public abstract void move();
}
