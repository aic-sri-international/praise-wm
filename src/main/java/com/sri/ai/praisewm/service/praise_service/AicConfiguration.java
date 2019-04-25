package com.sri.ai.praisewm.service.praise_service;

import com.sri.ai.expresso.ExpressoConfiguration;
import com.sri.ai.praise.PRAiSEConfiguration;

/**
 * AicConfiguration is used to set Expresso and PRAiSE configuration options.
 */
public class AicConfiguration {

  /**
   * Initialize configuration settings for Expresso and PRAiSE.
   *
   * <p>Settings are Thread-based.
   *
   * @param isInitialQuery
   */
  public static void initialize(boolean isInitialQuery) {
    if (isInitialQuery) {
      PRAiSEConfiguration.setUseUniformSamplingBackup(true);
    }

    ExpressoConfiguration.setDisplayNumericsExactlyForSymbols(false);
    ExpressoConfiguration
        .setDisplayNumericsMostDecimalPlacesInApproximateRepresentationOfNumericalSymbols(3);
  }
}
