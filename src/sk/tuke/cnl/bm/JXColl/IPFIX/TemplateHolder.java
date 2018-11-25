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
package sk.tuke.cnl.bm.JXColl.IPFIX;

import java.util.HashMap;

/**
 * Class used to hold templates for one UDP exporter identified by 
 * {@code <ipfixDevice, udpSourcePort, observationDomainId>} or just by observationDomainId
 * when connection oriented transport protocol is in use.
 * @author Tomas Verescak
 */
public class TemplateHolder {

    private HashMap<Integer, IPFIXTemplateRecord> templates;
    private static final int INITIAL_CAPACITY = 5;

    /**
     * Creates new instance with default initial capacity of 5.
     */
    public TemplateHolder() {
        templates = new HashMap<>(INITIAL_CAPACITY);
    }

    /**
     * Creates new instance with defined initial capacity.
     * @param capacity 
     */
    public TemplateHolder(int capacity) {
        templates = new HashMap<>(capacity);
    }

    /**
     * Adds template to this template holder.
     * @param template 
     */
    public void addTemplate(IPFIXTemplateRecord template) {
        templates.put(template.getTemplateID(), template);
    }

    /**
     * Tests whether templateholder contains template by given id.
     * @param templateId
     * @return 
     */
    public boolean contains(int templateId) {
        return templates.containsKey(templateId);
    }

    /**
     * Returns HashMap collection of all templates for this exporter.
     * @return HashMap<Integer, IPFIXTemplateRecord>
     */
    public HashMap<Integer, IPFIXTemplateRecord> getTemplates() {
        return templates;
    }

    /**
     * Retrieves template record given by template ID.
     * @param templateId Template ID 
     * @return IPFIXTemplateRecord template record 
     */
    public IPFIXTemplateRecord get(int templateId) {
        return templates.get(templateId);
    }

    /**
     * Removes template from cache by given template ID.
     * @param templateId Template ID, which is about to be removed
     * @return IPFIXTemplateRecord removed object.
     */
    public IPFIXTemplateRecord remove(int templateId) {
        IPFIXTemplateRecord template = templates.remove(templateId);
        // System.out.println("ContainsKey " + templateId + ": " + templates.containsKey(templateId));
        return template;
    }

    /**
     * Removes all templates for this exporter.
     */
    public void removeAll() {
        templates.clear();
    }

    /**
     * Tests whether this template holder is empty
     * @return 
     */
    public boolean isEmpty() {
        return templates.isEmpty();
    }
}
