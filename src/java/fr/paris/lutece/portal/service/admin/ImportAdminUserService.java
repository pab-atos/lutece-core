package fr.paris.lutece.portal.service.admin;

import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.business.user.AdminUserHome;
import fr.paris.lutece.portal.business.user.attribute.AdminUserField;
import fr.paris.lutece.portal.business.user.attribute.AdminUserFieldFilter;
import fr.paris.lutece.portal.business.user.attribute.AdminUserFieldHome;
import fr.paris.lutece.portal.business.user.attribute.IAttribute;
import fr.paris.lutece.portal.business.user.attribute.ISimpleValuesAttributes;
import fr.paris.lutece.portal.business.user.authentication.LuteceDefaultAdminUser;
import fr.paris.lutece.portal.business.workgroup.AdminWorkgroupHome;
import fr.paris.lutece.portal.service.csv.CSVMessageDescriptor;
import fr.paris.lutece.portal.service.csv.CSVMessageLevel;
import fr.paris.lutece.portal.service.csv.CSVReaderService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.user.attribute.AttributeService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.password.PasswordUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


/**
 * Class to import Admin Users from CSV files.
 */
public class ImportAdminUserService extends CSVReaderService
{

    private static final String MESSAGE_NO_LEVEL = "portal.users.import_users_from_file.importNoLevel";
    private static final String MESSAGE_NO_STATUS = "portal.users.import_users_from_file.importNoStatus";
    private static final String MESSAGE_ACCESS_CODE_ALREADY_USED = "portal.users.message.user.accessCodeAlreadyUsed";
    private static final String MESSAGE_EMAIL_ALREADY_USED = "portal.users.message.user.accessEmailUsed";
    private static final String MESSAGE_USERS_IMPORTED = "portal.users.import_users_from_file.usersImported";
    private static final String MESSAGE_ERROR_MIN_NUMBER_COLUMNS = "portal.users.import_users_from_file.messageErrorMinColumnNumber";

    private static final String PROPERTY_IMPORT_EXPORT_USER_SEPARATOR = "lutece.importExportUser.defaultSeparator";

    private static final String CONSTANT_DEFAULT_IMPORT_EXPORT_USER_SEPARATOR = ":";
    private static final String CONSTANT_RIGHT = "right";
    private static final String CONSTANT_ROLE = "role";
    private static final String CONSTANT_WORKGROUP = "workgroup";
    private static final int CONSTANT_MINIMUM_COLUMNS_PER_LINE = 12;

    private static final AttributeService _attributeService = AttributeService.getInstance( );

    private Character _strAttributesSeparator;
    private boolean _bUpdateExistingUsers = false;

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CSVMessageDescriptor> readLineOfCSVFile( String[] strLineDataArray, int nLineNumber, Locale locale )
    {
        List<CSVMessageDescriptor> listMessages = new ArrayList<CSVMessageDescriptor>( );
        int nIndex = 0;

        String strAccessCode = strLineDataArray[nIndex++];
        String strLastName = strLineDataArray[nIndex++];
        String strFirstName = strLineDataArray[nIndex++];
        String strEmail = strLineDataArray[nIndex++];

        boolean bUpdateUser = getUpdateExistingUsers( );
        int nUserId = 0;
        if ( bUpdateUser )
        {
            int nAccessCodeUserId = AdminUserHome.checkAccessCodeAlreadyInUse( strAccessCode );
            int nEmailUserId = AdminUserHome.checkEmailAlreadyInUse( strEmail );
            if ( nAccessCodeUserId > 0 )
            {
                nUserId = nAccessCodeUserId;
            }
            else if ( nEmailUserId > 0 )
            {
                nUserId = nEmailUserId;
            }
            bUpdateUser = nUserId > 0;
        }

        String strStatus = strLineDataArray[nIndex++];
        int nStatus = 0;
        if ( StringUtils.isNotEmpty( strStatus ) )
        {
            nStatus = Integer.parseInt( strStatus );
        }
        else
        {
            Object[] args = { strLastName, strFirstName, nStatus };
            String strMessage = I18nService.getLocalizedString( MESSAGE_NO_STATUS, args, locale );
            CSVMessageDescriptor message = new CSVMessageDescriptor( CSVMessageLevel.INFO, nLineNumber, strMessage );
            listMessages.add( message );
        }
        String strLocale = strLineDataArray[nIndex++];
        String strLevelUser = strLineDataArray[nIndex++];
        int nLevelUser = 3;
        if ( StringUtils.isNotEmpty( strLevelUser ) )
        {
            nLevelUser = Integer.parseInt( strLevelUser );
        }
        else
        {
            Object[] args = { strLastName, strFirstName, nLevelUser };
            String strMessage = I18nService.getLocalizedString( MESSAGE_NO_LEVEL, args, locale );
            CSVMessageDescriptor message = new CSVMessageDescriptor( CSVMessageLevel.INFO, nLineNumber, strMessage );
            listMessages.add( message );
        }
        // We ignore the rest password attribute because we set it to true anyway.
        // String strResetPassword = strLineDataArray[nIndex++];
        nIndex++;
        boolean bResetPassword = true;
        String strAccessibilityMode = strLineDataArray[nIndex++];
        boolean bAccessibilityMode = Boolean.parseBoolean( strAccessibilityMode );
        // We ignore the password max valid date attribute because we changed the password.
        // String strPasswordMaxValidDate = strLineDataArray[nIndex++];
        nIndex++;
        Timestamp passwordMaxValidDate = null;
        // We ignore the account max valid date attribute
        // String strAccountMaxValidDate = strLineDataArray[nIndex++];
        nIndex++;
        Timestamp accountMaxValidDate = AdminUserService.getAccountMaxValidDate( );
        String strDateLastLogin = strLineDataArray[nIndex++];
        Timestamp dateLastLogin = new Timestamp( AdminUser.DEFAULT_DATE_LAST_LOGIN.getTime( ) );
        if ( StringUtils.isNotBlank( strDateLastLogin ) )
        {
            long nLastLoginTime = Long.parseLong( strDateLastLogin );
            if ( nLastLoginTime > 0 )
            {
                dateLastLogin = new Timestamp( nLastLoginTime );
            }
        }
        AdminUser user = null;
        if ( bUpdateUser )
        {
            user = AdminUserHome.findByPrimaryKey( nUserId );
        }
        else
        {
            user = new LuteceDefaultAdminUser( );
        }
        user.setAccessCode( strAccessCode );
        user.setLastName( strLastName );
        user.setFirstName( strFirstName );
        user.setEmail( strEmail );
        user.setStatus( nStatus );
        user.setUserLevel( nLevelUser );
        user.setLocale( new Locale( strLocale ) );
        user.setAccessibilityMode( bAccessibilityMode );

        if ( bUpdateUser )
        {
            user.setUserId( nUserId );
            AdminUserHome.update( user );
            // We update the user
            AdminUserFieldFilter auFieldFilter = new AdminUserFieldFilter( );
            auFieldFilter.setIdUser( user.getUserId( ) );
            AdminUserFieldHome.removeByFilter( auFieldFilter );
        }
        else
        {
            // We create the user
            user.setPasswordReset( bResetPassword );
            user.setPasswordMaxValidDate( passwordMaxValidDate );
            user.setAccountMaxValidDate( accountMaxValidDate );
            user.setDateLastLogin( dateLastLogin );
            if ( AdminAuthenticationService.getInstance( ).isDefaultModuleUsed( ) )
            {
                LuteceDefaultAdminUser defaultAdminUser = (LuteceDefaultAdminUser) user;
                String strPassword = PasswordUtil.makePassword( );
                strPassword = AdminUserService.encryptPassword( strPassword );
                defaultAdminUser.setPassword( strPassword );
                AdminUserHome.create( defaultAdminUser );
            }
            else
            {
                AdminUserHome.create( user );
            }
        }
        AdminUserHome.removeAllRightsForUser( user.getUserId( ) );
        AdminUserHome.removeAllRolesForUser( user.getUserId( ) );

        List<IAttribute> listAttributes = _attributeService.getCoreAttributesWithoutFields( locale );
        Map<Integer, List<String>> mapAttributesValues = new HashMap<Integer, List<String>>( );

        // We get every attributes of the user
        List<String> listAdminRights = new ArrayList<String>( );
        List<String> listAdminRoles = new ArrayList<String>( );
        List<String> listAdminWorkgroups = new ArrayList<String>( );
        while ( nIndex < strLineDataArray.length )
        {
            String strValue = strLineDataArray[nIndex];
            if ( StringUtils.isNotBlank( strValue ) && strValue.indexOf( getAttributesSeparator( ) ) > 0 )
            {
                int nSeparatorIndex = strValue.indexOf( getAttributesSeparator( ) );
                String strLineId = strValue.substring( 0, nSeparatorIndex );
                if ( StringUtils.isNotBlank( strLineId ) )
                {
                    if ( StringUtils.equalsIgnoreCase( strLineId, CONSTANT_RIGHT ) )
                    {
                        listAdminRights.add( strValue.substring( nSeparatorIndex + 1 ) );
                    }
                    else if ( StringUtils.equalsIgnoreCase( strLineId, CONSTANT_ROLE ) )
                    {
                        listAdminRoles.add( strValue.substring( nSeparatorIndex + 1 ) );
                    }
                    else if ( StringUtils.equalsIgnoreCase( strLineId, CONSTANT_WORKGROUP ) )
                    {
                        listAdminWorkgroups.add( strValue.substring( nSeparatorIndex + 1 ) );
                    }
                    else
                    {
                        int nAttributeId = Integer.parseInt( strLineId );

                        String strAttributeValue = strValue.substring( nSeparatorIndex + 1 );
                        List<String> listValues = mapAttributesValues.get( nAttributeId );
                        if ( listValues == null )
                        {
                            listValues = new ArrayList<String>( );
                        }
                        listValues.add( strAttributeValue );
                        mapAttributesValues.put( nAttributeId, listValues );
                    }
                }
            }
            nIndex++;
        }

        // We save the attributes found
        for ( IAttribute attribute : listAttributes )
        {
            if ( attribute instanceof ISimpleValuesAttributes )
            {
                List<String> listValues = mapAttributesValues.get( attribute.getIdAttribute( ) );
                if ( listValues != null && listValues.size( ) > 0 )
                {
                    String[] strValues = new String[listValues.size( )];
                    int i = 0;
                    for ( String strValue : listValues )
                    {
                        strValues[i++] = strValue;
                    }
                    List<AdminUserField> listUserFields = ( (ISimpleValuesAttributes) attribute ).getUserFieldsData(
                            strValues, user );
                    for ( AdminUserField userField : listUserFields )
                    {
                        if ( userField != null )
                        {
                            AdminUserFieldHome.create( userField );
                        }
                    }
                }
            }
        }

        for ( String strRight : listAdminRights )
        {
            AdminUserHome.createRightForUser( user.getUserId( ), strRight );
        }

        for ( String strRole : listAdminRoles )
        {
            AdminUserHome.createRoleForUser( user.getUserId( ), strRole );
        }

        for ( String strWorkgoup : listAdminWorkgroups )
        {
            AdminWorkgroupHome.addUserForWorkgroup( user, strWorkgoup );
        }

        return listMessages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CSVMessageDescriptor> checkLineOfCSVFile( String[] strLineDataArray, int nLineNumber, Locale locale )
    {
        int nMinColumnNumber = CONSTANT_MINIMUM_COLUMNS_PER_LINE;
        List<CSVMessageDescriptor> listMessages = new ArrayList<CSVMessageDescriptor>( );
        if ( strLineDataArray == null || strLineDataArray.length < nMinColumnNumber )
        {
            Object[] args = { strLineDataArray.length, nMinColumnNumber };
            String strErrorMessage = I18nService.getLocalizedString( MESSAGE_ERROR_MIN_NUMBER_COLUMNS, args, locale );
            CSVMessageDescriptor error = new CSVMessageDescriptor( CSVMessageLevel.ERROR, nLineNumber, strErrorMessage );
            listMessages.add( error );
            return listMessages;
        }
        if ( !getUpdateExistingUsers( ) )
        {
            String strAccessCode = strLineDataArray[0];
            String strEmail = strLineDataArray[3];
            if ( AdminUserHome.checkAccessCodeAlreadyInUse( strAccessCode ) > 0 )
            {
                String strMessage = I18nService.getLocalizedString( MESSAGE_ACCESS_CODE_ALREADY_USED, locale );
                CSVMessageDescriptor error = new CSVMessageDescriptor( CSVMessageLevel.ERROR, nLineNumber, strMessage );
                listMessages.add( error );
            }
            else
            {
                if ( AdminUserHome.checkEmailAlreadyInUse( strEmail ) > 0 )
                {
                    String strMessage = I18nService.getLocalizedString( MESSAGE_EMAIL_ALREADY_USED, locale );
                    CSVMessageDescriptor error = new CSVMessageDescriptor( CSVMessageLevel.ERROR, nLineNumber,
                            strMessage );
                    listMessages.add( error );
                }
            }
        }

        return listMessages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CSVMessageDescriptor> getEndOfProcessMessages( int nNbLineParses, int nNbLinesWithoutErrors,
            Locale locale )
    {
        List<CSVMessageDescriptor> listMessages = new ArrayList<CSVMessageDescriptor>( );
        Object[] args = { nNbLineParses, nNbLinesWithoutErrors };
        String strMessageContent = I18nService.getLocalizedString( MESSAGE_USERS_IMPORTED, args, locale );
        CSVMessageDescriptor message = new CSVMessageDescriptor( CSVMessageLevel.INFO, 0, strMessageContent );
        listMessages.add( message );
        return listMessages;
    }

    /**
     * Get the separator used for attributes of admin users.
     * @return The separator
     */
    public Character getAttributesSeparator( )
    {
        if ( _strAttributesSeparator == null )
        {
            _strAttributesSeparator = AppPropertiesService.getProperty( PROPERTY_IMPORT_EXPORT_USER_SEPARATOR,
                    CONSTANT_DEFAULT_IMPORT_EXPORT_USER_SEPARATOR ).charAt( 0 );
        }
        return _strAttributesSeparator;
    }

    /**
     * Get the update users flag
     * @return True if existing users should be updated, false if they should be
     *         ignored.
     */
    public boolean getUpdateExistingUsers( )
    {
        return _bUpdateExistingUsers;
    }

    /**
     * Set the update users flag
     * @param bUpdateExistingUsers True if existing users should be updated,
     *            false if they should be ignored.
     */
    public void setUpdateExistingUsers( boolean bUpdateExistingUsers )
    {
        this._bUpdateExistingUsers = bUpdateExistingUsers;
    }

}
