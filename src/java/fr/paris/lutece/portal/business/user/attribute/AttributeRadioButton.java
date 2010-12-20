/*
 * Copyright (c) 2002-2010, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.portal.business.user.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.web.constants.Messages;


/**
 *
 *  class EntryTypeRadioButton
 *
 */
public class AttributeRadioButton extends AbstractAttribute
{
	// Constants
    private static final String EMPTY_STRING = "";
    private static final String CONSTANT_UNDERSCORE = "_";

    // Parameters
    private static final String PARAMETER_TITLE = "title";
    private static final String PARAMETER_HELP_MESSAGE = "help_message";
    private static final String PARAMETER_MANDATORY = "mandatory";
    private static final String PARAMETER_IS_SHOWN_IN_SEARCH = "is_shown_in_search";
    private static final String PARAMETER_ATTRIBUTE = "attribute";
    private static final String PARAMETER_IS_FIELD_IN_LINE = "is_field_in_line";
    
	// Properties
    private static final String PROPERTY_TYPE_RADIO_BUTTON = "portal.users.attribute.type.radioButton";
    private static final String PROPERTY_CREATE_RADIO_BUTTON_PAGETITLE = "portal.users.create_attribute.pageTitleAttributeRadioButton";
    private static final String PROPERTY_MODIFY_RADIO_BUTTON_PAGETITLE = "portal.users.modify_attribute.pageTitleAttributeRadioButton";
	
	// Templates
	private static final String TEMPLATE_CREATE_ATTRIBUTE = "admin/user/attribute/radiobutton/create_attribute_radio_button.html";
    private static final String TEMPLATE_MODIFY_ATTRIBUTE = "admin/user/attribute/radiobutton/modify_attribute_radio_button.html";
    private static final String TEMPLATE_HTML_FORM_ATTRIBUTE = "admin/user/attribute/radiobutton/html_code_form_attribute_radio_button.html";
    private static final String TEMPLATE_HTML_FORM_SEARCH_ATTRIBUTE = "admin/user/attribute/radiobutton/html_code_form_search_attribute_radio_button.html";
    /**
     * Constructor
     */
    public AttributeRadioButton(  )
    {
    }
    
    /**
     * Get the template create an attribute
     * @return The URL of the template
     */
    public String getTemplateCreateAttribute(  )
    {
        return TEMPLATE_CREATE_ATTRIBUTE;
    }

    /**
     * Get the template modify an attribute
     * @return The URL of the template
     */
    public String getTemplateModifyAttribute(  )
    {
        return TEMPLATE_MODIFY_ATTRIBUTE;
    }

    /**
     * Get the template html form attribute
     * @return the template
     */
    public String getTemplateHtmlFormAttribute(  )
    {
        return TEMPLATE_HTML_FORM_ATTRIBUTE;
    }

    /**
     * Get the template html form search attribute
     * @return the template
     */
    public String getTemplateHtmlFormSearchAttribute(  )
    {
        return TEMPLATE_HTML_FORM_SEARCH_ATTRIBUTE;
    }

    /**
     * Get page title for create page
     * @return page title
     */
    public String getPropertyCreatePageTitle(  )
    {
        return PROPERTY_CREATE_RADIO_BUTTON_PAGETITLE;
    }

    /**
     * Get page title for modify page
     * @return page title
     */
    public String getPropertyModifyPageTitle(  )
    {
        return PROPERTY_MODIFY_RADIO_BUTTON_PAGETITLE;
    }

    /**
     * Set the data of the attribute
     * @param request HttpServletRequest
     * @return null if there are no errors
     */
    public String setAttributeData( HttpServletRequest request )
    {
        String strTitle = request.getParameter( PARAMETER_TITLE );
        String strHelpMessage = ( request.getParameter( PARAMETER_HELP_MESSAGE ) != null )
            ? request.getParameter( PARAMETER_HELP_MESSAGE ).trim(  ) : null;
        String strIsShownInSearch = request.getParameter( PARAMETER_IS_SHOWN_IN_SEARCH );
        String strMandatory = request.getParameter( PARAMETER_MANDATORY );
        String strFieldInLine = request.getParameter( PARAMETER_IS_FIELD_IN_LINE );

        if ( ( strTitle == null ) || ( strTitle.equals( EMPTY_STRING ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        setTitle( strTitle );
        setHelpMessage( strHelpMessage );
        setMandatory( strMandatory != null );
        setShownInSearch( strIsShownInSearch != null );
        setFieldInLine( strFieldInLine != null );

        return null;
    }

    /**
     * Set attribute type
     * @param locale locale
     */
    public void setAttributeType( Locale locale )
    {
        AttributeType attributeType = new AttributeType(  );
        attributeType.setLocale( locale );
        attributeType.setClassName( this.getClass(  ).getName(  ) );
        attributeType.setLabelType( PROPERTY_TYPE_RADIO_BUTTON );
        setAttributeType( attributeType );
    }

    /**
     * Get the data of the user fields
     * @param request HttpServletRequest
     * @param user user
     * @return user field data
     */
    public List<AdminUserField> getUserFieldsData( HttpServletRequest request, AdminUser user )
    {
        String value = request.getParameter( PARAMETER_ATTRIBUTE + CONSTANT_UNDERSCORE + _nIdAttribute );
        List<AdminUserField> listUserFields = new ArrayList<AdminUserField>(  );
        AdminUserField userField = new AdminUserField(  );
        AttributeField attributeField;

        if ( StringUtils.isNotBlank( value ) )
        {
        	int nIdField = Integer.parseInt( value );
            attributeField = AttributeFieldHome.findByPrimaryKey( nIdField );
            if ( attributeField != null )
            {
            	userField.setUser( user );
                userField.setAttribute( this );
                userField.setAttributeField( attributeField );
                userField.setValue( attributeField.getTitle(  ) );
                listUserFields.add( userField );
            }
        }
        
        return listUserFields;
    }
}
