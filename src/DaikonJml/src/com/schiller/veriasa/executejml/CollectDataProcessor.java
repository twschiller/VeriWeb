package com.schiller.veriasa.executejml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import daikon.FileIO;
import daikon.PptMap;
import daikon.PptTopLevel;
import daikon.ValueTuple;

/**
 * Populates the <tt>samples</tt> map with all the data read from the file.
 * This is only reasonable for small trace files, since all the data will
 * be retained in memory!
 */
public class CollectDataProcessor extends FileIO.Processor {

  public Map<PptTopLevel,List<ValueTuple>> samples = new LinkedHashMap<PptTopLevel,List<ValueTuple>>();

  /** Process the sample, by adding it to the <tt>samples</tt> map. */
  public void process_sample (PptMap all_ppts, PptTopLevel ppt,
                              ValueTuple vt, Integer nonce) {

    // Add orig and derived variables to the ValueTuple
    FileIO.add_orig_variables(ppt, vt.vals, vt.mods, nonce);
    FileIO.add_derived_variables(ppt, vt.vals, vt.mods);

    // Intern the sample, to save space, since we are storing them all.
    vt = new ValueTuple(vt.vals, vt.mods);

    // Add the sample to the map
    if (! samples.containsKey(ppt)) {
      samples.put(ppt, new ArrayList<ValueTuple>());
    }
    samples.get(ppt).add(vt);
  }
}