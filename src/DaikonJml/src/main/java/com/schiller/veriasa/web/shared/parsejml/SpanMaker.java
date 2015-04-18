package com.schiller.veriasa.web.shared.parsejml;

import java.util.List;

public interface SpanMaker {
	
	String makeSpan(JmlSpan f, List<JmlSpan> associated);
}
