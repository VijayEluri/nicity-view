/*
 * VPan.java.java
 *
 * Created on 01-03-2010 01:33:52 PM
 *
 * Copyright 2010 Jonathan Colt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.colt.nicity.view.core;

import com.colt.nicity.view.border.ViewBorder;
import com.colt.nicity.view.event.AInputEvent;
import com.colt.nicity.view.event.AViewEvent;
import com.colt.nicity.view.event.MouseDragged;
import com.colt.nicity.view.event.MouseEntered;
import com.colt.nicity.view.event.MouseExited;
import com.colt.nicity.view.event.MouseMoved;
import com.colt.nicity.view.event.MousePressed;
import com.colt.nicity.view.event.MouseReleased;
import com.colt.nicity.view.event.MouseWheel;
import com.colt.nicity.view.flavor.ScrollFlavor;
import com.colt.nicity.view.paint.UPaint;
import com.colt.nicity.core.lang.ICallback;
import com.colt.nicity.core.lang.MinMaxInt;
import com.colt.nicity.core.lang.UDouble;
import com.colt.nicity.core.lang.UFloat;
import com.colt.nicity.core.memory.struct.XYWH_I;
import com.colt.nicity.core.memory.struct.XY_I;
import com.colt.nicity.view.interfaces.ICanvas;
import com.colt.nicity.view.interfaces.IDrop;
import com.colt.nicity.view.interfaces.IDropMode;
import com.colt.nicity.view.interfaces.IEvent;
import com.colt.nicity.view.interfaces.IMouseEvents;
import com.colt.nicity.view.interfaces.IMouseMotionEvents;
import com.colt.nicity.view.interfaces.IMouseWheelEvents;
import com.colt.nicity.view.interfaces.IView;
import java.awt.geom.GeneralPath;

/**
 *
 * @author Administrator
 */
public class VPan extends VClip implements IDrop, IMouseWheelEvents, IMouseEvents, IMouseMotionEvents {

    static ScrollFlavor flavor = new ScrollFlavor();
    float maxWBeforePan = -1;
    float maxHBeforePan = -1;
    float fixedW = -1;
    float fixedH = -1;
    /**
     *
     */
    public int scrollBarSize = 16;
    /**
     *
     */
    public boolean operable = true;
    /**
     *
     */
    public ICallback dropCallback;
    /**
     *
     */
    public ICallback droppedCallback;
    /**
     *
     */
    public AColor barColor = ViewColor.cThemeScroll;
    /**
     *
     */
    public boolean resizeable = true;
    /**
     *
     */
    protected boolean resizing = false;
    /**
     *
     */
    protected boolean scrollingX = false;
    /**
     *
     */
    protected boolean scrollingY = false;
    private boolean pan = false;
    private boolean paintYScrollbar = false;
    private boolean paintXScrollbar = false;

    /**
     *
     * @param _view
     * @param _w
     * @param _h
     * @param _autoCenterX
     * @param _autoCenterY
     */
    public VPan(
            IView _view,
            float _w, float _h,
            boolean _autoCenterX, boolean _autoCenterY) {
        this(_view, _w, _h);
        setAutoCenter(_autoCenterX, _autoCenterY);
    }

    /**
     *
     * @param _view
     * @param _w
     * @param _h
     * @param _autoCenter
     */
    public VPan(IView _view, float _w, float _h, boolean _autoCenter) {
        this(_view, _w, _h);
        setAutoCenter(_autoCenter);
    }

    /**
     *
     * @param _view
     * @param _w
     * @param _h
     */
    public VPan(IView _view, float _w, float _h) {
        super(_view, _w, _h);
        setBorder(new ViewBorder());
    }

    /**
     *
     * @param _barColor
     */
    public void setBarColor(AColor _barColor) {
        barColor = _barColor;
    }

    @Override
    public synchronized IView spans(int spanMasks) {
        return this;
    }

    @Override
    public void mend() {
        enableFlag(UV.cRepair);//??
        super.mend();
    }

    /**
     *
     * @param _autoCenter
     */
    public void setAutoCenter(boolean _autoCenter) {
        autoCenterX = _autoCenter;
        autoCenterY = _autoCenter;
    }

    /**
     *
     * @param _autoCenterX
     * @param _autoCenterY
     */
    public void setAutoCenter(boolean _autoCenterX, boolean _autoCenterY) {
        autoCenterX = _autoCenterX;
        autoCenterY = _autoCenterY;
    }

    /**
     *
     * @param _w
     * @param _h
     */
    public void setSizeBeforeScroll(int _w, int _h) {
        maxWBeforePan = _w;
        maxHBeforePan = _h;
    }

    /**
     *
     * @param _w
     * @param _h
     */
    public void fixedSize(int _w, int _h) {
        fixedW = _w;
        fixedH = _h;
        if (fixedW != -1) {
            w = fixedW;
        }
        if (fixedH != -1) {
            h = fixedH;
        }
    }

    @Override
    public void paintBody(ICanvas g, Layer _layer, int _mode, XYWH_I _painted) {
        super.paintBody(g, _layer, _mode, _painted);
        if (operable) {
            if (alignY > -1 && view.getH() > getH()) {//paintYScrollbar
                XYWH_I yr = panY();
                _painted.union((int) (_layer.x() + yr.x), (int) (_layer.y() + yr.y), yr.w, yr.h);
            }
            if (alignX > -1 && view.getW() > getW()) {//paintXScrollbar
                XYWH_I xr = panX();
                _painted.union((int) (_layer.x() + xr.x), (int) (_layer.y() + xr.y), xr.w, xr.h);
            }
        }
    }

    @Override
    public void paintBorder(ICanvas _g, int _x, int _y, int _w, int _h) {
        super.paintBorder(_g, _x, _y, _w, _h);
        if (operable) {
            IView view = placer.getView();
            if (alignY > -1 && view.getH() > getH()) {//paintYScrollbar

                _g.setColor(barColor);
                _g.line(_x + _w - (scrollBarSize / 2), _y, _x + _w - (scrollBarSize / 2), _y + _h);


                XYWH_I r = panY();
                if (scrollingY || paintYScrollbar) {
                    flavor.paintFlavor(_g, _x + r.x, _y + r.y, r.w, r.h, ViewColor.cThemeActive);
                } else {
                    flavor.paintFlavor(_g, _x + r.x, _y + r.y, r.w, r.h, barColor);
                }

                flavor.paintFlavor(_g, _x + _w - scrollBarSize, _y + 0, scrollBarSize, scrollBarSize, barColor);
                flavor.paintFlavor(_g, _x + _w - scrollBarSize, _y + _h - scrollBarSize - scrollBarSize, scrollBarSize, scrollBarSize, barColor);
                _g.setColor(ViewColor.cThemeFont);
                GeneralPath p = new GeneralPath();
                UPaint.arrowHead(p, _x + _w - (scrollBarSize / 2), _y + (scrollBarSize / 2), 0, scrollBarSize / 3, (float) 90);
                _g.draw(p);
                p = new GeneralPath();
                UPaint.arrowHead(p, _x + _w - (scrollBarSize / 2), _y + _h - scrollBarSize - (scrollBarSize / 2), (float) 180, scrollBarSize / 3, (float) 90);
                _g.draw(p);

            }
            if (alignX > -1 && view.getW() > getW()) {//paintXScrollbar

                _g.setColor(barColor);
                _g.line(_x, _y + _h - (scrollBarSize / 2), _x + _w, _y + _h - (scrollBarSize / 2));

                XYWH_I r = panX();
                if (scrollingX || paintXScrollbar) {
                    flavor.paintFlavor(_g, _x + r.x, _y + r.y, r.w, r.h, ViewColor.cThemeActive);
                } else {
                    flavor.paintFlavor(_g, _x + r.x, _y + r.y, r.w, r.h, barColor);
                }
                flavor.paintFlavor(_g, _x + 0, _y + _h - scrollBarSize, scrollBarSize, scrollBarSize, barColor);
                flavor.paintFlavor(_g, _x + _w - scrollBarSize - scrollBarSize, _y + _h - scrollBarSize, scrollBarSize, scrollBarSize, barColor);

                _g.setColor(ViewColor.cThemeFont);
                GeneralPath p = new GeneralPath();
                UPaint.arrowHead(p, _x + (scrollBarSize / 2), _y + _h - (scrollBarSize / 2), 90, scrollBarSize / 3, (float) 90);
                _g.draw(p);
                p = new GeneralPath();
                UPaint.arrowHead(p, _x + _w - scrollBarSize - (scrollBarSize / 2), _y + _h - (scrollBarSize / 2), (float) 270, scrollBarSize / 3, (float) 90);
                _g.draw(p);
            }
            if (resizeable && (alignX > -1 || alignY > -1)) {
                flavor.paintFlavor(_g, _x + _w - scrollBarSize, _y + _h - scrollBarSize, scrollBarSize, scrollBarSize, barColor);
                _g.setColor(ViewColor.cThemeFont);
                _g.oval(false, _x + _w + (scrollBarSize / 2) - (scrollBarSize / 4), _y + _h + (scrollBarSize / 2) - (scrollBarSize / 4), (scrollBarSize / 2), (scrollBarSize / 2));
            }
        }
    }

    /**
     *
     * @return
     */
    public XYWH_I incUp() {
        float _w = getW();
        return new XYWH_I(_w - scrollBarSize, 0, scrollBarSize, scrollBarSize);
    }

    /**
     *
     * @return
     */
    public XYWH_I incDown() {
        float _w = getW();
        float _h = getH();
        return new XYWH_I(_w - scrollBarSize, _h - scrollBarSize - scrollBarSize, scrollBarSize, scrollBarSize);
    }

    /**
     *
     * @return
     */
    public XYWH_I incRight() {
        float _w = getW();
        float _h = getH();
        return new XYWH_I(_w - scrollBarSize - scrollBarSize, _h - scrollBarSize, scrollBarSize, scrollBarSize);
    }

    /**
     *
     * @return
     */
    public XYWH_I incLeft() {
        float _h = getH();
        return new XYWH_I(0, _h - scrollBarSize, scrollBarSize, scrollBarSize);
    }

    /**
     *
     * @return
     */
    public XYWH_I panY() {
        float _h = getH() - (scrollBarSize * 3);// top botton and resize areas = scrollBarSize*3
        int _y = (int) (alignY * _h);
        int ph = 0;
        if (hSlack != 0) {
            ph = (int) ((_h / (_h + hSlack)) * _h);
        }
        if (ph < scrollBarSize) {
            ph = scrollBarSize;
        }
        return new XYWH_I((int) (getW() - scrollBarSize), scrollBarSize + (int) (_y - (ph * alignY)), scrollBarSize, ph);
    }

    /**
     *
     * @return
     */
    public XYWH_I panX() {
        float _w = getW() - (scrollBarSize * 3);// top botton and resize areas = scrollBarSize*3
        int _x = (int) (alignX * _w);
        int pw = 0;
        if (wSlack != 0) {
            pw = (int) ((_w / (_w + wSlack)) * _w);
        }
        if (pw < scrollBarSize) {
            pw = scrollBarSize;
        }
        return new XYWH_I(scrollBarSize + (int) (_x - (pw * alignX)), (int) (getH() - scrollBarSize), pw, scrollBarSize);
    }

    @Override
    public void paintBackground(ICanvas _g, int _x, int _y, int _w, int _h) {
        if (maxWBeforePan > -1 && maxWBeforePan > view.getW()) {
            //alignX = -1;
            w = view.getW();
            parent.layoutInterior();
            parent.repair();
            parent.flush();
        } else if (maxWBeforePan > -1 && alignX < 0) {
            w = maxWBeforePan;
            alignX = 0;
            parent.layoutInterior();
            parent.repair();
            parent.flush();
        }
        if (maxHBeforePan > -1 && maxHBeforePan > view.getH()) {
            //alignY = -1;
            h = view.getH();
            parent.layoutInterior();
            parent.repair();
            parent.flush();
        } else if (maxHBeforePan > -1 && alignY < 0) {
            h = maxHBeforePan;
            alignY = 0;
            parent.layoutInterior();
            parent.repair();
            parent.flush();
        }
        super.paintBackground(_g, _x, _y, _w, _h);

    }

    @Override
    public IView disbatchEvent(IView parent, AViewEvent event) {
        if (isPanEvent(event) || scrollingX || scrollingY) {
            return this;
        }
        if (event instanceof MouseMoved) {
            XY_I p = ((MouseMoved) event).getPoint();
            if ((alignX > -1 && p.x > getW() - scrollBarSize) || (alignY > -1 && p.y > getH() - scrollBarSize)) {
                setPaintingScrollBars(p, isPanEvent(event));
                return this;//??

            } else {
                paintXScrollbar = false;
                paintYScrollbar = false;
                return super.disbatchEvent(parent, event);
            }
        } else if (event instanceof MousePressed) {
            XY_I p = ((MousePressed) event).getPoint();
            setModePoint(p, isPanEvent(event));
            if (p.x > getW() - scrollBarSize && p.y > getH() - scrollBarSize) {
                setPan(false);
                return this;
            }
            if (scrollingX || scrollingY) {
                return this;
            } else {
                return super.disbatchEvent(parent, event);
            }
        } else if (event instanceof MouseReleased) {
            XY_I p = ((MouseReleased) event).getPoint();
            setModePoint(p, isPanEvent(event));
            if (p.x > getW() - scrollBarSize && p.y > getH() - scrollBarSize) {
                setPan(false);
                return this;
            }
            if (scrollingX || scrollingY) {
                return this;
            } else {
                return super.disbatchEvent(parent, event);
            }
        } else {
            return super.disbatchEvent(parent, event);
        }
    }

    boolean isPanEvent(IEvent event) {
        if (event instanceof AInputEvent) {
            boolean isPanEvent = (((AInputEvent) event).isShiftDown() && ((AInputEvent) event).isControlDown());
            if (isPanEvent) {
                return isPanEvent;
            }
        }
        lmp = null;
        return false;
    }

    // IMouseWheelEvents
    public void mouseWheel(MouseWheel _e) {
        int rotation = _e.getWheelRotation();
        if (_e.isShiftDown()) {
            if (rotation < 0) {
                for (int i = rotation; i < 0; i++) {
                    incRightScroll();
                }
            } else {
                for (int i = 0; i < rotation; i++) {
                    incLeftScroll();
                }
            }
        } else {
            if (rotation < 0) {
                for (int i = rotation; i < 0; i++) {
                    incUpScroll();
                }
            } else {
                for (int i = 0; i < rotation; i++) {
                    incDownScroll();
                }
            }
        }
    }

    /**
     *
     */
    protected void incUpScroll() {
        float p = getAlignY() - (((float) getH() / 3) / ((float) getContent().getH()));
        setPositionY(UFloat.checkFloat(p, 0.0f));
    }

    /**
     *
     */
    protected void incDownScroll() {
        float p = getAlignY() + (((float) getH() / 3) / ((float) getContent().getH()));
        setPositionY(UFloat.checkFloat(p, 1.0f));
    }

    /**
     *
     */
    protected void incLeftScroll() {
        float p = getAlignX() - (((float) getW() / 3) / ((float) getContent().getW()));
        setPositionX(UFloat.checkFloat(p, 0.0f));
    }

    /**
     *
     */
    protected void incRightScroll() {
        float p = getAlignX() + (((float) getW() / 3) / ((float) getContent().getW()));
        setPositionX(UFloat.checkFloat(p, 1.0f));
    }

    /**
     *
     * @param _position
     */
    public void setPositionX(float _position) {
        if (_position < 0.0f) {
            _position = 0.0f;
        }
        if (_position > 1.0f) {
            _position = 1.0f;
        }
        setAlignX(_position, this);
        paint();
    }

    /**
     *
     * @param _position
     */
    public void setPositionY(float _position) {
        if (_position < 0.0f) {
            _position = 0.0f;
        }
        if (_position > 1.0f) {
            _position = 1.0f;
        }
        setAlignY(_position, this);
        paint();
    }

    /**
     *
     * @return
     */
    public float getPositionX() {
        return getAlignX();
    }

    /**
     *
     * @return
     */
    public float getPositionY() {
        return getAlignY();
    }

    /**
     *
     * @param _pan
     */
    public void setPan(boolean _pan) {
        pan = _pan;
        //grabFocus();
        //getRootView().setMouseWheelFocus(0,this);//!!
    }

    // IMouseEvents
    public void mouseEntered(MouseEntered _e) {
        DragAndDrop.cDefault.mouseEntered(_e);
    }

    public void mouseExited(MouseExited _e) {
        DragAndDrop.cDefault.mouseExited(_e);
    }

    public void mousePressed(MousePressed _e) {
        if (_e.getClickCount() > 0) {
            DragAndDrop.cDefault.mousePressed(_e);
        }
        setModePoint(_e.getPoint(), isPanEvent(_e));
    }

    /**
     *
     * @param _p
     * @param _panXY
     */
    public void setModePoint(XY_I _p, boolean _panXY) {
        if (_panXY) {
            scrollingX = true;
            scrollingY = true;
        } else if (resizeable && (_p.x > getW() - scrollBarSize && _p.y > getH() - scrollBarSize)) {
            resizing = true;
            scrollingX = false;
            scrollingY = false;
        } else if (panY().contains(_p.x, _p.y) || incRight().contains(_p.x, _p.y) || incLeft().contains(_p.x, _p.y)) {
            resizing = false;
            scrollingX = false;
            scrollingY = true;
        } else if (panX().contains(_p.x, _p.y) || incUp().contains(_p.x, _p.y) || incDown().contains(_p.x, _p.y)) {
            resizing = false;
            scrollingX = true;
            scrollingY = false;
        } else {
            resizing = false;
            scrollingX = false;
            scrollingY = false;
        }
    }

    public void mouseReleased(MouseReleased _e) {
        PickupAndDrop.cDefault.event(_e);
        if (_e.getClickCount() > 0) {
            DragAndDrop.cDefault.mouseReleased(_e);
            if (_e.getClickCount() == 1) {
                int _w = (int) getW();
                int _h = (int) getH();

                XY_I p = _e.getPoint();
                if (p.x > _w - scrollBarSize && p.x < _w) { // vertical inc dec
                    if (p.y > 0 && p.y < scrollBarSize) {
                        incUpScroll();
                    }
                    if (p.y > _h - scrollBarSize - scrollBarSize && p.y < _h - scrollBarSize) {
                        incDownScroll();
                    }
                }
                if (p.y > _h - scrollBarSize && p.y < _h) { // horizontal inc dec
                    if (p.x > 0 && p.x < scrollBarSize) {
                        incLeftScroll();
                    }
                    if (p.x > _w - scrollBarSize - scrollBarSize && p.x < _w - scrollBarSize) {
                        incRightScroll();
                    }
                }

            }
        }
        resizing = false;
        scrollingX = false;
        scrollingY = false;
        lmp = null;
    }

    // IMouseMotionEvents
    /**
     *
     * @param _p
     * @param _pan
     */
    public void setPaintingScrollBars(XY_I _p, boolean _pan) {

        if (_p.x > getW() - scrollBarSize || _pan) {
            if (paintYScrollbar); else {
                getRootView().setMouseWheelFocus(0, this);//!!
                paintYScrollbar = true;
                paint();
            }
        } else {
            paintYScrollbar = false;
            paint();
        }

        if (_p.y > getH() - scrollBarSize || _pan) {
            if (paintXScrollbar); else {
                getRootView().setMouseWheelFocus(0, this);//!!
                paintXScrollbar = true;
                paint();
            }
        } else {
            paintXScrollbar = false;
            paint();
        }
    }
    XY_I lmp = null;

    public void mouseMoved(MouseMoved _e) {
        XY_I p = _e.getPoint();
        setPaintingScrollBars(p, isPanEvent(_e));
        if (isPanEvent(_e)) {
            if (lmp == null) {
                lmp = _e.getPoint();
                return;
            }
            XY_I mp = _e.getPoint();

            float yrate = (lmp.y - mp.y) / (getH() / 2);
            setPositionY(getPositionY() - yrate);
            float xrate = (lmp.x - mp.x) / (getW() / 2);
            setPositionX(getPositionX() - xrate);

            lmp = mp;
        }
    }

    public void mouseDragged(MouseDragged _e) {
        DragAndDrop.cDefault.mouseDragged(_e);
        if (!operable) {
            return;
        }
        if (resizeable && resizing) {

            if (fixedW == -1) {
                w += _e.getDeltaX();
            }
            if (fixedH == -1) {
                h += _e.getDeltaY();
            }
            layoutInterior();
            parent.layoutInterior();
            parent.paint();
        }
        if (scrollingY) {
            XYWH_I py = panY();
            int hh = py.h / 2;
            MinMaxInt mm = new MinMaxInt();
            mm.value(hh);
            mm.value((int) (getH() - hh));
            double position = mm.zeroToOne(_e.getPoint().y);
            position = UDouble.clamp(position, 0, 1);
            setPositionY((float) position);
        }
        if (scrollingX) {
            XYWH_I px = panX();
            int hw = px.w / 2;
            MinMaxInt mm = new MinMaxInt();
            mm.value(hw);
            mm.value((int) (getW() - hw));
            double position = mm.zeroToOne(_e.getPoint().x);
            position = UDouble.clamp(position, 0, 1);
            setPositionX((float) position);
        }
    }

    // IDrop
    public IDropMode accepts(Object value, AInputEvent _e) {
        if (dropCallback == null) {
            return null;
        }
        return (IDropMode) dropCallback.callback(new Object[]{value, _e});
    }

    public void dropParcel(final Object value, final IDropMode mode) {
        if (droppedCallback == null) {
            return;
        }
        new Thread() {// refactor add to drop callstack instead
            @Override
            public void run() {
                droppedCallback.callback(new Object[]{value, mode});
            }
        }.start();
    }
}
