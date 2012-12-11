/*******************************************************************************
 * Copyright (c) 2012 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.archive.vtype;

import org.epics.pvmanager.data.VEnum;
import org.epics.pvmanager.data.VNumberArray;
import org.epics.pvmanager.data.VType;
import org.epics.util.array.ListNumber;

/** Formatter for {@link VType} values that uses strings for some numeric arrays
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class StringVTypeFormat extends DefaultVTypeFormat
{
    
    // TODO CAtch VNumberArray, display as text
    
    /** {@inheritDoc} */
    public StringBuilder format(final VType value, final StringBuilder buf)
    {
        if (value instanceof VEnum)
        {
            final VEnum enumerated = (VEnum)value;
            try
            {
                buf.append(enumerated.getValue());
            }
            catch (ArrayIndexOutOfBoundsException ex)
            {   // Error getting label for invalid index?
                buf.append("<enum ").append(enumerated.getIndex()).append(">");
            }
        }
        else if (value instanceof VNumberArray)
        {
            final ListNumber numbers = ((VNumberArray)value).getData();
            final int N = numbers.size();
            for (int i=0; i<N; ++i)
            {
                final int code = numbers.getInt(i);
                if (code == 0)
                    break;
                formatChar(code, buf);
            }
        }
        else
            super.format(value, buf);
        return buf;
    }
    
    private void formatChar(final int code, final StringBuilder buf)
    {
        // Ideally, this was some combination of
        // if (Character.isAlphabetic(code) ||
        //     Character.isSpaceChar(code) ...
        // but wasn't sure what to use
        if (code >= 32 // 'Space' or ASCII that follows
         || code == 8  // Backspace
         || code == 9  // Tab
         || code == 10 // NL
         || code == 13 // CR
           )
            buf.append(Character.toChars(code));
        else
            buf.append(String.format("\\u%04d", code));
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "String";
    }
}