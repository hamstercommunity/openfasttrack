package openfasttrack.cli;

/*-
 * #%L
 * OpenFastTrack
 * %%
 * Copyright (C) 2016 - 2017 hamstercommunity
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


public enum ExitStatus
{
    OK(0), FAILURE(1), CLI_ERROR(2);

    private int code;

    ExitStatus(final int code)
    {
        this.code = code;
    }

    int getCode()
    {
        return this.code;
    }

    public static ExitStatus fromBoolean(final boolean status)
    {
        return status ? OK : FAILURE;
    }
}
