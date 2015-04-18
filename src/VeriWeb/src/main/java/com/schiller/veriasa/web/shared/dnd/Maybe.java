package com.schiller.veriasa.web.shared.dnd;

/**
 * Option values
 * @author Todd Schiller
 */
public interface Maybe<T>{
	boolean hasValue();
	T getValue();
}
