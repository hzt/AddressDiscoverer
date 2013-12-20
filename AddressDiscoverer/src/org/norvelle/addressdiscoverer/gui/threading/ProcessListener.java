/*
 * Copyright (C) 2013 Erik Norvelle <erik.norvelle@cyberlogos.co>
 *
 * An interface for classes that listen for updates on processes
 */

package org.norvelle.addressdiscoverer.gui.threading;

import java.util.EventListener;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public interface ProcessListener extends EventListener {
    void processFinished(Process process);
}