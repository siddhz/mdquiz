package g.qmq;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class animToolBox {
	public final static int ZOOM_IN_CENTER = 0;
	
	public animToolBox() {
	}

	/**
	 * Quick animation
	 * @param mode ZOOM_IN_CENTER
	 * @param v View
	 */
	public animToolBox(int mode) {
		switch(mode){
		case 0:
			scaleMode = Animation.RELATIVE_TO_SELF;
			s1 = 0.5f;
			s2 = 0.5f;
			x1 = 1;
			x2 = 0;
			y1 = 1;
			y2 = 0;
			duration = 1000;
			return;
		}
	}
	
	/**
	 * 动画设置
	 * 
	 * @param v
	 *            View 控制View
	 * @param fx
	 *            float X开始
	 * @param tx
	 *            float X结束
	 * @param fy
	 *            float y开始
	 * @param ty
	 *            float y结束
	 */
	public animToolBox(float fx, float tx, float fy, float ty) {
		x1 = fx;
		x2 = tx;
		y1 = fy;
		y2 = ty;
	}

	/**
	 * 快速移进/出 执行
	 * 
	 * @param v
	 *            View 控制View
	 * @param direction
	 *            char 方向上下左右(t,b,l,r)
	 * @param out
	 *            boolean 移出屏幕.
	 */
	public animToolBox(View v, char direction, boolean out) {
		setDirection(direction, out);
		animMove(v);
	}

	/**
	 * 快速移进/出 设置
	 * 
	 * @param direction
	 *            char 方向上下左右(t,b,l,r)
	 * @param out
	 *            boolean 移出屏幕.
	 */
	public animToolBox(char direction, boolean out) {
		setDirection(direction, out);
	}

	/* 移动动画区 START */
	/**
	 * 方向设置
	 * 
	 * @param direction
	 *            char 方向上下左右(t,b,l,r)
	 * @param out
	 *            boolean 移出屏幕.
	 */
	public void setDirection(char direction, boolean out) {
		switch (direction) {
		case 'l':
			if (out) {
				x2 = -300;
			} else {
				x1 = -300;
			}
			break;
		case 'r':
			if (out) {
				x2 = 300;
			} else {
				x1 = 300;
			}
			break;
		case 't':
			if (out) {
				y2 = 500;
			} else {
				y1 = 500;
			}
			break;
		case 'b':
			if (out) {
				y2 = -500;
			} else {
				y1 = -500;
			}
			break;
		}
	}

	/**
	 * 执行移动
	 * 
	 * @param v
	 *            View 控件
	 */
	public void animMove(View v) {
		Animation anim = null;
		anim = new TranslateAnimation(x1, x2, y1, y2);
		anim.setInterpolator(new AccelerateDecelerateInterpolator());
		anim.setDuration(duration);
		anim.setFillAfter(fill);
		anim.setStartOffset(delate);
		v.startAnimation(anim);
	}

	/* 移动动画区 END */

	/* 缩放动画区 START */
	/**
	 * 设置缩放模式
	 * 
	 * @param mode
	 *            int Animation.ABSOLUTE; Animation.RELATIVE_TO_SELF;
	 *            Animation.RELATIVE_TO_PARENT;
	 */
	public void setScaleMode(int mode) {
		scaleMode = mode;
	}

	/**
	 * 执行缩放
	 * 
	 * @param v
	 *            View 控件
	 */
	public void animScale(View v) {
		Animation anim = null;
		anim = new ScaleAnimation(x1, x2, y1, y2, scaleMode, s1, scaleMode, s2);
		anim.setInterpolator(new AccelerateDecelerateInterpolator());
		anim.setDuration(duration);
		anim.setFillAfter(fill);
		anim.setStartOffset(delate);
		v.startAnimation(anim);
	}

	public void setS(float sS1, float sS2) {
		s1 = sS1;
		s2 = sS2;
	}

	/* 缩放动画区 END */

	/**
	 * 延迟开始
	 * 
	 * @param sDelate
	 *            long 延迟时间(毫秒)
	 */
	public void setDelate(long sDelate) {
		delate = sDelate;
	}

	/**
	 * 设置动画持续长度
	 * 
	 * @param nDuratio
	 *            long 毫秒
	 */
	public void setTime(long nDuration) {
		duration = nDuration;
	}

	/**
	 * 设置动画持续长度
	 * 
	 * @param nDuratio
	 *            long 毫秒
	 */
	public void setFill(boolean sfill) {
		fill = sfill;
	}

	public void setXY(float sX1, float sX2, float sY1, float sY2) {
		x1 = sX1;
		x2 = sX2;
		y1 = sY1;
		y2 = sY2;
	}

	private int scaleMode = 0;
	private float x1 = 0, x2 = 0, y1 = 0, y2 = 0, s1 = 0, s2 = 0;
	private long duration = 1000, delate = 0;
	private boolean fill = true;
	public Animation AM;
}
