/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.quantasy.smarthome;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class Color {

    private float r;
    private float g;
    private float b;

    public Color() {
    }

    public Color(float r, float g, float b) {
	this.r = r;
	this.g = g;
	this.b = b;
    }

    public void setB(float b) {
	this.b = b;
    }

    public void setG(float g) {
	this.g = g;
    }

    public void setR(float r) {
	this.r = r;
    }

    public float getB() {
	return b;
    }

    public float getG() {
	return g;
    }

    public float getR() {
	return r;
    }

}
