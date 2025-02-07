package com.example.wan_try.dglab.sync;

import com.example.wan_try.dglab.MinecraftDgLabContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Reactive<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Reactive.class);
    private T value;
    private final Side sideA;

    private final Side sideB;

    public Side getSideB() {
        return sideB;
    }

    public Side getSideA() {
        return sideA;
    }


    public Reactive(T value) {
        this.sideA = this.new Side();
        this.sideB = this.new Side();
        bind(sideA,sideB);
        this.value = value;
        LOGGER.debug("Created new Reactive with initial value: {}", value);
    }

    public T get(){
        return value;
    }

    private void bind(Side sideA,Side sideB){
        this.sideA.bindTo(sideB);
        this.sideB.bindTo(sideA);
        LOGGER.debug("Bound sides A and B");
    }

    public class Side {
        private final List<Consumer<T>> onValueChanged;
        private Side other;

        private void bindTo(Side other){
            this.other = other;
            LOGGER.debug("Side bound to other side");
        }

        public Side() {
            onValueChanged = new ArrayList<>();
            LOGGER.debug("Created new Side");
        }

        public T get(){
            return Reactive.this.get();
        }

        public final void update(T val){
            LOGGER.debug("Updating value from {} to {}", value, val);
            value = val;
            if(other != null){
                LOGGER.debug("Notifying {} listeners on other side", other.onValueChanged.size());
                other.onValueChanged.forEach(consumer -> consumer.accept(val));
            }
        }

        public void onUpdate(Consumer<T> onValueChanged){
            this.onValueChanged.add(onValueChanged);
            LOGGER.debug("Added value change listener, total listeners: {}", this.onValueChanged.size());
        }
    }
}
