/*
 * Copyright (C) 2012 Tomas Verescak
 *
 * This file is part of JXColl.
 *
 * JXColl is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.

 * JXColl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with JXColl; If not, see <http://www.gnu.org/licenses/>.
 */
package sk.tuke.cnl.bm;

/**
 * Exception related with IPFIX template handling. This
 * exception might be thrown when there is attempt to withdraw non-existent
 * template or when trying to add template into cache if it is already there
 * (when using TCP or SCTP as transport protocol)
 * @author Tomas Verescak
 */
public class TemplateException extends JXCollException {

    public TemplateException(String message) {
        super(message);
    }
}
