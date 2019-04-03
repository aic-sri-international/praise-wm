package com.sri.ai.praisewm.service.praise_service;

import com.sri.ai.expresso.ExpressoConfiguration;
import com.sri.ai.praise.PRAiSEConfiguration;

public class AicConfiguration {

  /**
   * Initialize configuration settings for Expresso and PRAiSE.
   * <p>
   *   Settings are Thread-based.
   * </p>
   *
   * @param isInitialQuery
   */
  public static void initialize(boolean isInitialQuery) {
    PRAiSEConfiguration.setUseUniformSamplingBackup(isInitialQuery);
    ExpressoConfiguration.setDisplayNumericsExactlyForSymbols(false);
    ExpressoConfiguration
        .setDisplayNumericsMostDecimalPlacesInApproximateRepresentationOfNumericalSymbols(3);
  }
}
