package com.schiller.veriasa.web.shared.executejml;

import java.util.List;

public interface ValSpanMaker {
	String makeSpan(ValFragment f, String tooltip, List<ValFragment> associated);
}
