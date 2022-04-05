package servlet.access;

import constants.Constants;
import constants.ErrorCodes;
import dao.emailconfermation.InsertEmailConfermationDatabase;
import dao.person.GetUserByEmailDatabase;
import dao.person.GetUserByTaxCodeDatabase;
import dao.person.InsertNewUserDatabase;
import dao.person.InsertUserRoleDatabase;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import resource.EmailConfermation;
import resource.Message;
import resource.Person;
import servlet.AbstractServlet;
import utils.EncryptionManager;
import utils.InputValidation;
import utils.MailWrapper;

import javax.naming.NamingException;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;


/**
 * Da testare max size upload!!!
 *
 * */
public class RegisterServlet extends AbstractServlet
{


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        req.getRequestDispatcher(Constants.PATH_REGISTER).forward(req, res);
    }
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException
    {
        String taxCode = null;
        String firstName = null;
        String lastName = null;
        String address = null;
        String email = null;
        String password= null;
        String telephoneNumber = null;
        Date birthDate = null;
        Part avatar = null;


        boolean registrable = true;
        Message message = new Message(ErrorCodes.OK.getErrorMessage(),false);
        ErrorCodes error = parseParams(req,res);


        if(error.getErrorCode() != ErrorCodes.OK.getErrorCode())
        {
            message = new Message(error.getErrorMessage(),true);
            registrable = false;
        }

        if(registrable) // it means that params are ok for now
        {
            taxCode = req.getParameter(Constants.TAX_CODE);
            firstName = req.getParameter(Constants.FIRST_NAME);
            lastName = req.getParameter(Constants.LAST_NAME);
            address = req.getParameter(Constants.ADDRESS);
            email = req.getParameter(Constants.EMAIL);
            password = req.getParameter(Constants.PASSWORD);
            telephoneNumber = req.getParameter(Constants.TELEPHONE_NUMBER);
            birthDate = Date.valueOf(req.getParameter(Constants.BIRTH_DATE));
            avatar = req.getPart(Constants.AVATAR);

            //insertUser
            error = insertUser(taxCode,firstName,lastName,address,email,password,telephoneNumber,birthDate,avatar,Person.ROLE_TRAINEE);
            if(error.getErrorCode() != ErrorCodes.OK.getErrorCode())
            {
                message = new Message(error.getErrorMessage(),true);
                registrable = false;
            }
        }

        res.setStatus(error.getHTTPCode());
        req.setAttribute(Constants.MESSAGE,message);

        if(!registrable)
            req.getRequestDispatcher(Constants.PATH_REGISTER).forward(req, res);
        else
            req.getRequestDispatcher(Constants.PATH_CONFIRM_REGISTRATION).forward(req, res);

    }


    public ErrorCodes parseParams(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        String taxCode = null;
        String firstName = null;
        String lastName = null;
        String address = null;
        String email = null;
        String password= null;
        String confirmPassword= null;
        String telephoneNumber = null;
        Date birthDate = null;

        ErrorCodes error = ErrorCodes.OK;
        try
        {
            taxCode = req.getParameter(Constants.TAX_CODE);
            firstName = req.getParameter(Constants.FIRST_NAME);
            lastName = req.getParameter(Constants.LAST_NAME);
            address = req.getParameter(Constants.ADDRESS);
            email = req.getParameter(Constants.EMAIL);
            password = req.getParameter(Constants.PASSWORD);
            confirmPassword = req.getParameter(Constants.CONFIRM_PASSWORD);
            telephoneNumber = req.getParameter(Constants.TELEPHONE_NUMBER);
            Integer.parseInt(telephoneNumber);
            birthDate = Date.valueOf(req.getParameter(Constants.BIRTH_DATE));

        }catch ( IllegalArgumentException e) //Either Telephone isn't a telephone or birthDate isn't a Date
        {
            error = ErrorCodes.INVALID_FIELDS;
        }

        if(error.getErrorCode() == ErrorCodes.OK.getErrorCode()) //Phone is a phone and birthDate is a Date
        {
            if(taxCode == null || taxCode.isEmpty() ||
                    firstName == null || firstName.isEmpty() ||
                    lastName == null || lastName.isEmpty() ||
                    address == null || address.isEmpty() ||
                    email == null || email.isEmpty() ||
                    password == null || password.isEmpty() ||
                    confirmPassword == null || confirmPassword.isEmpty() ||
                    telephoneNumber == null || telephoneNumber.isEmpty() ||
                    birthDate == null) // Check if some of the fields are empty
            {
                error = ErrorCodes.EMPTY_INPUT_FIELDS;
            }else if(!password.equals(confirmPassword)) //all fields are not empty => check if the meaning is ok
            {
                error = ErrorCodes.DIFFERENT_PASSWORDS;
            }else if(telephoneNumber.length() != Person.LENGTH_TELEPHONE)
            {
                error = ErrorCodes.NOT_TELEPHONE_NUMBER;
            }else if ((Period.between(LocalDate.parse(birthDate.toString()), LocalDate.now()).getYears() < Person.MIN_AGE))
            {
                error = ErrorCodes.MINIMUM_AGE;
            }else if(!InputValidation.isValidEmailAddress(email))
            {
                error = ErrorCodes.NOT_A_MAIL;
            }
        }
        return error;
    }

    public ErrorCodes insertUser(String taxCode,String firstName, String lastName,String address,String email,
                                 String password, String telephoneNumber,Date birthDate,Part avatar, String role)
    {
        ErrorCodes error = ErrorCodes.OK;
        Person p1 = null;
        Person p2 = null;
        try
        {
            p1 = (new GetUserByEmailDatabase(getDataSource().getConnection(), email)).execute();

            p2 = (new GetUserByTaxCodeDatabase(getDataSource().getConnection(),new Person(null,null,null,
                    null,taxCode,null,null,null,null))).execute();

            if(p1 == null || p2 == null)//It's a new user, so need to add it !
            {

                try
                {
                    error = saveFile(avatar,taxCode);
                    if(error.getErrorCode() == ErrorCodes.OK.getErrorCode())
                    {
                        //File saved ok can proceed with writing on the database and send email and log to test max dimension of the file??
                        String pathImg = null;
                        if((avatar != null) && (avatar.getSize() != 0)) //.png da aggiungere
                            pathImg = Constants.AVATAR_PATH_FOLDER + File.separator + taxCode +
                                    File.separator + Constants.AVATAR +"."+ avatar.getContentType().split(File.separator)[1];
                        try
                        {
                            Person p = new Person(email,firstName,lastName, EncryptionManager.encrypt(password)
                                    ,taxCode,birthDate,telephoneNumber,address,pathImg);


                            //QUI DA FINIRE
                            //Insert User into Database
                            new InsertNewUserDatabase(getDataSource().getConnection(),p).execute();
                            new InsertUserRoleDatabase(getDataSource().getConnection(),p,role).execute();

                            //Send email
                            String msg = "please confirm your email at this address : " + Constants.CONFIRMATION_URL + EncryptionManager.encrypt(email);

                            //Insert Expiration Date Into database
                            (new InsertEmailConfermationDatabase(getDataSource().getConnection(), new EmailConfermation(p.getEmail(),EncryptionManager.encrypt(email),
                                    new Timestamp(System.currentTimeMillis() + Constants.DAY)))).execute();

                            //TODO


                            MailWrapper mw = new MailWrapper();
                            mw.getManager().sendMail(p.getEmail(),"WELCOME TO GWA : CONFIRM YOUR REGISTRATION",msg);

                            //MailManager2.sendMail("WELCOME TO GWA : CONFIRM YOUR REGISTRATION", msg, p);


                        } catch (NoSuchAlgorithmException | MessagingException e)
                        {
                            error = ErrorCodes.INTERNAL_ERROR;
                        }
                    }
                }catch (IOException e)
                {
                    error = ErrorCodes.INTERNAL_ERROR;
                }

            }else
                error = ErrorCodes.USER_ALREADY_PRESENT;
        }catch (SQLException | NamingException e )
        {
            error = ErrorCodes.INTERNAL_ERROR;
        }
        return error;
    }


    private ErrorCodes saveFile(Part file, String taxCode) throws IOException
    {
        ErrorCodes error = ErrorCodes.OK;
        File createDirectory = null;
        OutputStream writer = null;
        InputStream content = null;
        String path = null;
        //Logger log = LogManager.getLogger("francesco_caldivezzi_logger");

        if((file != null) && file.getSize() != 0)
        {
            if(!Arrays.stream(Constants.ACCPETED_EXTENSIONS_AVATAR).
                    anyMatch(file.getContentType().split(File.separator)[1]::equals))
                error = ErrorCodes.INVALID_FILE_TYPE;
            else
                path = Constants.AVATAR_PATH_FOLDER + File.separator + taxCode;

            if(error.getErrorCode() == ErrorCodes.OK.getErrorCode()) //can proceed to save
            {
                createDirectory = new File(path);
                if(!createDirectory.exists())
                    createDirectory.mkdir();

                path = path + File.separator + Constants.AVATAR + "." + file.getContentType().split(File.separator)[1];
                //log.info(path);
                try
                {
                    writer = new FileOutputStream(path);
                    content = file.getInputStream();
                    int read = 0;
                    final byte[] bytes = new byte[1024];
                    while ((read = content.read(bytes)) != -1)
                        writer.write(bytes, 0, read);
                }catch (IOException e)
                {
                    error = ErrorCodes.CANNOT_UPLOAD_FILE;
                }finally
                {
                    if(content != null)
                        content.close();
                    if(writer != null)
                        writer.close();
                }
            }
        }
        return error;
    }

}