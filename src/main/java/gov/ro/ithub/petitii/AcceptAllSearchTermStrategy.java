package gov.ro.ithub.petitii;

import org.springframework.integration.mail.SearchTermStrategy;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.search.SearchTerm;

/**
 * Created by ciprian.pavel on 11/24/2016.
 */
public class AcceptAllSearchTermStrategy implements SearchTermStrategy {

    public SearchTerm generateSearchTerm(Flags flags, Folder folder) {
        return new AcceptAllSearchTerm();
    }

    private class AcceptAllSearchTerm extends SearchTerm {
       public boolean match(Message mesg){
            return true;
        }
    }
}
