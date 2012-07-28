/**
 * Retail Planner Class for Cloudspokes Challenge #1650
 */
public class RetailPlanner {

    /**
     * Clones promotion event records for the specified planning session
     * The input is the _current_ planning session, so records will be cloned
     * for the _next_ half-year period from the previous year's historical data
     *
     * @param year    The year of current planning session
     * @param half    The identifier of first or second half of the year
     * @return        A boolean indicating the success of cloning
     */
    public static Boolean cloneSession(Integer year, Integer half) {
        System.debug('Cloning session for ' + half + 'H' + year + ' from historical records from ' + half + 'H' + (year-1));
        
        // setup the save point for rollback
        Savepoint sp = Database.setSavepoint();
        
        try {
        
            // Calculate the next planning session
            half++;
            if (half == 3) {
                half = 1;
                year++;
            }
            
            // Calculate start and end dates for previous year's planning session
            Datetime startDate, endDate;
            if (half == 1) {
                startDate = Datetime.newInstanceGmt(year-1,1,1);
                endDate = Datetime.newInstanceGmt(year-1,7,1);
            } else {
                startDate = Datetime.newInstanceGmt(year-1,7,1);
                endDate = Datetime.newInstanceGmt(year,1,1);
            }
            
            // Query every promotional event from previous year, clone and increment the year
            for (Promotional_Event__c event : [SELECT name, account__c, promotional_campaign__c, startdatetime__c, enddatetime__c FROM promotional_event__c WHERE startdatetime__c >= :startDate AND enddatetime__c < :endDate]) {
                Promotional_Event__c clonedEvent = event.clone(false, true);
                clonedEvent.startdatetime__c = clonedEvent.startdatetime__c.addYears(1);
                clonedEvent.enddatetime__c = clonedEvent.enddatetime__c.addYears(1);
                insert clonedEvent;
            }
        
        } catch (Exception e){
             // roll everything back in case of error
            Database.rollback(sp);
            ApexPages.addMessages(e);
            return false;
        }
        
        return true;
    }
    
    private static void setupTestEvents() {
        Promotional_Campaign__c campaign;
        Promotional_Event__c event;
        Account account;
        
        account = new Account(name = 'Macy\'s New York');
        insert account;
        campaign = new Promotional_Campaign__c(name = 'Cash Harvest', type__c = 'Resource');
        insert campaign;
        event = new Promotional_Event__c(name = 'Event #1 2H2010', account__c = account.ID, promotional_campaign__c = campaign.ID, startdatetime__c = datetime.newInstanceGmt(2010,12,30), enddatetime__c = datetime.newInstanceGmt(2010,12,31));
        insert event;
        event = new Promotional_Event__c(name = 'Event #2 1H2011', account__c = account.ID, promotional_campaign__c = campaign.ID, startdatetime__c = datetime.newInstanceGmt(2011,1,1), enddatetime__c = datetime.newInstanceGmt(2011,1,1));
        insert event;
        event = new Promotional_Event__c(name = 'Event #3 1H2011', account__c = account.ID, promotional_campaign__c = campaign.ID, startdatetime__c = datetime.newInstanceGmt(2011,6,30), enddatetime__c = datetime.newInstanceGmt(2011,6,30));
        insert event;
        event = new Promotional_Event__c(name = 'Event #4 2H2011', account__c = account.ID, promotional_campaign__c = campaign.ID, startdatetime__c = datetime.newInstanceGmt(2011,7,1), enddatetime__c = datetime.newInstanceGmt(2011,7,1));
        insert event;
        event = new Promotional_Event__c(name = 'Event #5 2H2011', account__c = account.ID, promotional_campaign__c = campaign.ID, startdatetime__c = datetime.newInstanceGmt(2011,12,31), enddatetime__c = datetime.newInstanceGmt(2011,12,31));
        insert event;
        event = new Promotional_Event__c(name = 'Event #6 1H2012', account__c = account.ID, promotional_campaign__c = campaign.ID, startdatetime__c = datetime.newInstanceGmt(2012,1,1), enddatetime__c = datetime.newInstanceGmt(2012,1,2));
        insert event;
    }
    
    static testMethod void testCloneSessionFirstHalf() {
        // given the test input
        setupTestEvents();
                
        // when cloning for 1H2012 (current session is 2H2011)
        Boolean success = cloneSession(2011, 2);
        System.assert(success);
        
        // then
        Promotional_Event__c[] events;
        
        events = [select startdatetime__c, enddatetime__c from promotional_event__c where name = 'Event #1 2H2010'];
        System.assertEquals(1, events.size());

        events = [select startdatetime__c, enddatetime__c from promotional_event__c where name = 'Event #2 1H2011'];
        System.assertEquals(2, events.size());
        System.assertEquals(datetime.newInstanceGmt(2012,1,1), events[1].startdatetime__c);
        System.assertEquals(datetime.newInstanceGmt(2012,1,1), events[1].enddatetime__c);

        events = [select startdatetime__c, enddatetime__c from promotional_event__c where name = 'Event #3 1H2011'];
        System.assertEquals(2, events.size());
        System.assertEquals(datetime.newInstanceGmt(2012,6,30), events[1].startdatetime__c);
        System.assertEquals(datetime.newInstanceGmt(2012,6,30), events[1].enddatetime__c);

        events = [select startdatetime__c, enddatetime__c from promotional_event__c where name = 'Event #4 2H2011'];
        System.assertEquals(1, events.size());

        events = [select startdatetime__c, enddatetime__c from promotional_event__c where name = 'Event #5 2H2011'];
        System.assertEquals(1, events.size());
        
        events = [select startdatetime__c, enddatetime__c from promotional_event__c where name = 'Event #6 1H2012'];
        System.assertEquals(1, events.size());
    }

    static testMethod void testCloneSessionSecondHalf() {
        // given the test input
        setupTestEvents();
 
        // when cloning for 2H2012 (current session is 1H2012)
        Boolean success = cloneSession(2012, 1);
        System.assert(success);
        
        // then
        Promotional_Event__c[] events;
                
        events = [select startdatetime__c, enddatetime__c from promotional_event__c where name = 'Event #1 2H2010'];
        System.assertEquals(1, events.size());

        events = [select startdatetime__c, enddatetime__c from promotional_event__c where name = 'Event #2 1H2011'];
        System.assertEquals(1, events.size());

        events = [select startdatetime__c, enddatetime__c from promotional_event__c where name = 'Event #3 1H2011'];
        System.assertEquals(1, events.size());

        events = [select startdatetime__c, enddatetime__c from promotional_event__c where name = 'Event #4 2H2011'];
        System.assertEquals(2, events.size());
        System.assertEquals(datetime.newInstanceGmt(2012,7,1), events[1].startdatetime__c);
        System.assertEquals(datetime.newInstanceGmt(2012,7,1), events[1].enddatetime__c);

        events = [select startdatetime__c, enddatetime__c from promotional_event__c where name = 'Event #5 2H2011'];
        System.assertEquals(2, events.size());
        System.assertEquals(datetime.newInstanceGmt(2012,12,31), events[1].startdatetime__c);
        System.assertEquals(datetime.newInstanceGmt(2012,12,31), events[1].enddatetime__c);
        
        events = [select startdatetime__c, enddatetime__c from promotional_event__c where name = 'Event #6 1H2012'];
        System.assertEquals(1, events.size());
    }
    
}