/*
 * AVMode.java.java
 *
 * Created on 03-12-2010 06:40:42 PM
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
package colt.nicity.view.value;

import colt.nicity.view.list.AItem;
import colt.nicity.core.observer.AObserver;
import colt.nicity.core.observer.Change;
import colt.nicity.core.observer.IObservable;
import colt.nicity.core.observer.IObserver;
import colt.nicity.core.value.Value;
import colt.nicity.view.interfaces.IEvent;
import colt.nicity.view.interfaces.IView;

/**
 *
 * @author Administrator
 */
abstract public class AVMode extends AItem {

    /**
     *
     * @param _index
     * @return
     */
    abstract public IView indexView(int _index);

    /**
     *
     * @return
     */
    abstract public int maxIndex();
    Value<Integer> value;
    IObserver observer;

    /**
     *
     * @param _value
     */
    public AVMode(Value<Integer> _value) {
        value = _value;
        observer = new AObserver() {

            @Override
            public void change(Change _change) {
                refresh();
            }

            public void bound(IObservable _observable) {
                refresh();
            }

            public void released(IObservable _observable) {
            }
        };
        value.bind(observer);
    }

    /**
     *
     * @param _e
     */
    @Override
    public void picked(IEvent _e) {
        int index = value.intValue();
        if (index + 1 < maxIndex()) {
            value.setValue(index + 1);
        }
        else {
            value.setValue(0);
        }
    }

    /**
     *
     * @return
     */
    public int getMode() {
        return value.intValue();
    }

    /**
     *
     */
    public void refresh() {
        setView(indexView(value.intValue()));
        paint();
    }
}
