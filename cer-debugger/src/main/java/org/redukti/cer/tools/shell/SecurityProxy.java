/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.redukti.cer.tools.shell;

import org.redukti.cer.Scriptable;
import org.redukti.cer.runtime.Context;
import org.redukti.cer.runtime.SecurityController;

public abstract class SecurityProxy extends SecurityController {
    protected abstract void callProcessFileSecure(Context cx, Scriptable scope, String filename);
}
