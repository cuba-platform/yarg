package com.haulmont.yarg.reporting.extraction;

import com.haulmont.yarg.loaders.factory.ReportLoaderFactory;
import com.haulmont.yarg.structure.BandOrientation;

import java.util.function.BiFunction;

/**
 * This interface implementation may holding relation between report band orientation and related controller logic
 * if relation not set, default controller should be returned
 * <p>The default controller implementation is <b>com.haulmont.yarg.reporting.extraction.controller.DefaultExtractionController</b></p>
 *
 * <p>The default implementation is <b>com.haulmont.yarg.reporting.extraction.DefaultExtractionControllerFactory</b></p>
 */
public interface ExtractionControllerFactory {
    /**
     * Method for runtime configuring data extraction logic by orientation
     *
     * @param orientation band orientation
     * @param controllerCreator specific creator function for extraction controller
     */
    void register(BandOrientation orientation, BiFunction<ExtractionControllerFactory, ReportLoaderFactory, ExtractionController> controllerCreator);

    /**
     * @param orientation band orientation
     * @return data extraction controller
     */
    ExtractionController controllerBy(BandOrientation orientation);

    /**
     * @return default data extraction controller
     */
    ExtractionController defaultController();
}
