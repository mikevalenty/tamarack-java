Tamarack is a micro framework for implementing the Chain of Responsibility pattern in Java
=================================================================================================

The Chain of Responsibility is a key building block of extensible software.

>Avoid coupling the sender of a request to its receiver by giving more than one object a 
>chance to handle the request. Chain the receiving objects and pass the request along the 
>chain until an object handles it. -- Gang of Four

Show me examples!
-----------
Consider a block of code to process a blog comment coming from a web-based rich text editor. There are
probably several things you'll want to do before letting the text into your database. 

```java
public class BlogEngine {
  ...

  public int submit(Post post) {

	Pipeline<Post, Integer> pipeline = new Pipeline<Post, Integer>()
		.add(new CanonicalizeHtml())
		.add(new StripMaliciousTags())
		.add(new RemoveJavascript())
		.add(new RewriteProfanity())
		.add(new GuardAgainstDoublePost())
		.add(new SaveNewPost()));

	int newId = pipeline.execute(post);

	return newId;
  }
}
```

How about user login? There are all kinds of things you might need to do there:

```java
public class LoginService {
  ...

  public boolean login(string username, string password) {

	FilterFactory factory = new GuiceFilterFactory(injector);

	Pipeline<LoginContext, Boolean pipeline = new Pipeline<LoginContext, Boolean>(factory)
		.add(WriteLoginAttemptToAuditLog.class)
		.add(LockoutOnConsecutiveFailures.class)
		.add(AuthenticateAgainstLocalStore.class)
		.add(AuthenticateAgainstLdap.class);

	return pipeline.execute(new LoginContext(username, password));
  }
}
```

Calculating a spam score in a random block of text:

```java
public class SpamScorer {
  ...

  public double CalculateSpamScore(string text) {

	double score = new Pipeline<String, Double>()
		.add(SpamCopBlacklistFilter.class)
		.add(PrescriptionDrugFilter.class)
		.add(PornographyFilter.class);
		.execute(text);
  }
}
```

How does it work?
-----------

It's pretty simple, there is just one interface to implement and it looks like this:

```java
public interface Filter<T, TOut> {

  boolean canExecute(T context);

  TOut execute(T context, Filter<T, TOut> next);
}
```

Basically, you get an input to operate on and you return a value. The `next` parameter 
is the next filter in the chain and using it in this fashion allows you several options:

 * Modify the input before the next filter gets it
 * Modify the output of the next filter before returning
 * Short circuit out of the chain by not calling the executeNext delegate

I learn by example, so let's look at this interface in action. In the spam score calculator 
example, each filter looks for markers in the text and adds to the overall spam score by
modifying the _result_ of the next filter before returning.

```java
public class PrescriptionDrugFilter extends AbstractFilter<String, Double> {

  @Override
  public Double execute(String text, Filter<String, Double> next) {

	Double score = next.execute(text, next);

	if (text.contains("viagra")) {
	  score += .25;
	}

	return score;
  }
}
```

In this login example, we're look for the user in our local user store and if it exists 
we'll short-circuit the chain and authenticate the request. Otherwise we'll let the request 
continue to the next filter which looks for the user in an Ldap respository.

```java
public class AuthenticateAgainstLocalStore extends AbstractFilter<LoginContext, Boolean> {
  ...

  public Boolean execute(LoginContext context, Filter<LoginContext, Boolean> next) {

	User user = repository.findByUsername(context.getUsername());

	if (user != null) {
	  return user.isValid(context.getPassword()); // short circuit
	}

	return next.execute(context, next);
  }
}
```