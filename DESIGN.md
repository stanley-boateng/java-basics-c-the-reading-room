# Design notes: Rescue an Inherited Codebase: The Reading Room

These notes explain how the rebuild is structured, why I chose each part over the
alternatives, and how the structure makes the change the library says is coming
(e-loans) easy to add. The diagnosis of the original program is in
[`DIAGNOSIS.md`](DIAGNOSIS.md).

## Redesigned structure in one picture

```
com.amalitech.training.readingroom
├── LendingTracker        entry point: sets up the library and replays the afternoon
├── item/                 WHAT can be lent
│   ├── LendingItem       abstract: every item has a title and a loan rule
│   ├── Book              14 days standard / 28 student
│   ├── AudioKit          7 days standard / 14 student
│   └── LoanPeriod        record: the two loan lengths for one kind of item
├── member/               WHO borrows
│   ├── Member            name + membership type
│   └── MemberType        enum: STANDARD, STUDENT
├── library/              THE RULES
│   ├── Library           the only place loans are opened and closed
│   ├── Loan              record: one loan, never changed after it is created
│   └── ItemAlreadyOnLoanException
├── desk/                 WHAT THE VOLUNTEER SEES
│   └── LendingDesk       prints the same messages as the original
└── report/
    └── EndOfDayReport    builds the end-of-day report lines
```

Each package answers a question. That's the main thing that makes the code safe for
a volunteer to change: to change a loan rule you open `item/`, to change a message you
open `desk/`, and the lending rules in `library/` stay untouched.

---

## Decision 1: one place answers "who has this item?"

**The problem.** The original stored "who has this item" twice: on the item
(`status`, `currentBorrower`) and in the list of loans. `borrow()` updated one and
appended to the other without checking either. That's how an item ended up on two
loans at once, and how the report came to contradict itself.

**What I did.** `Library` keeps a single private map from each item to its active loan
(`activeLoanIndexByItem`). Every question about an item's status goes through it:
- `borrow()` checks the map first. If the item is already there, it throws
  `ItemAlreadyOnLoanException` and nothing is changed.
- `returnItem()` removes that item's entry and closes **only that loan**.
- The report asks `library.isOnLoan(item)`. Items have no status field, so the item
  section and the "Active loans" section can never disagree.

**The alternative I rejected.** Keeping a `status` field on each item and remembering
to update it alongside the loan. That's the original design. It depends on every
future change updating two places in step, which is exactly what went wrong.

## Decision 2: each kind of item owns its loan rule

**The problem.** The original had one `borrow()` method with an `if` branch per item
type, matched on free-text strings (`"BOOK"`, `"AUDIO_KIT"`). Changing the audio-kit
rules meant editing the same method books depended on, and a misspelt type such as
`"book"` silently created a 0-day loan.

**What I did.** `LendingItem` is an abstract class holding what every item shares: a
title, and the check that the title isn't blank. `Book` and `AudioKit` extend it, and
each one supplies its own `typeCode()` and `loanPeriod()`. `Loan.open` simply asks:

```java
int days = item.loanPeriod().daysFor(member.type());
```

`Library` never checks *which* kind of item it has. This is polymorphism: each item
answers for itself. Changing the audio-kit rule now means editing `AudioKit.java`
only, and book code is never opened. A misspelt type can't happen any more, because
item kinds are classes the compiler checks.

**The alternatives I considered:**

| Alternative | Why I didn't choose it |
|---|---|
| An `ItemType` enum (`BOOK(14, 28)`, `AUDIO_KIT(7, 14)`) stored on a single item class | Simpler, with every loan rule in one file. But every new kind means editing that shared file, and if e-loans have their own *behaviour* (not just different numbers), it turns into `if (type == …)` checks spread around the code. That's how the original became fragile. |
| Records for `Book` and `AudioKit` | Records compare by value, so two copies of *Things Fall Apart* would count as the same item. Returning one would affect the other, which is the original's return bug again. Items need to be separate objects. |
| Keep the original single class and just add checks | Fixes today's bug, but leaves the shared `if`/`else` method that made changes risky. |

**Composition as well as inheritance.** Items *inherit* their title and its
validation, but each item *has a* `LoanPeriod` (composition). The rule is a small
value that's easy to read and test on its own.

## Decision 3: nothing outside `Library` can put the records into an invalid state

- **Private, final fields and no setters** in every class. Once created, an item,
  member or loan can't be changed from outside.
- **Constructors validate input.** A blank title or name, a missing type, a zero-day
  loan period or a return date before the borrow date is rejected immediately, so an
  invalid object can never exist.
- **Only `Library` opens and closes loans.** `Loan.open` and `Loan.close` are
  package-private, so only code in the `library` package can call them.
- **Read-only lists.** `items()`, `members()`, `activeLoans()`, `overdueLoans()` and
  `loansFor()` return `List.copyOf(...)`. A caller can read the data, but
  `.clear()` or `.add()` throws an exception instead of changing the library's
  records.
- **Registration is checked.** Lending an item or member that was never added to the
  library is rejected.

## Decision 4: values are records, things with an identity are classes

- `Loan` and `LoanPeriod` are **records**: plain, unchangeable data. Returning an item
  doesn't edit the loan; it creates a new `Loan` with the return date filled in.
- `LendingItem` and `Member` are **classes** on purpose. Two members called Kofi
  Mensah are two people, and two copies of a book are two items. Classes keep Java's
  default "same object" equality, so the library never confuses them.

## Decision 5: a refused loan is a checked exception

`ItemAlreadyOnLoanException` extends `Exception`, so the compiler forces every caller
of `borrow()` to deal with it. An item being out is a normal situation at the desk,
not a programming mistake. Making it checked means no future caller can forget it,
which is exactly what the original did. `LendingDesk` catches it and tells the
volunteer who has the item.

Programming mistakes, such as passing `null` or an unregistered item, use Java's
built-in unchecked exceptions (`NullPointerException`, `IllegalArgumentException`),
because they mean the calling code is wrong, not that something happened at the desk.

**Alternative rejected:** returning `false` or `null` from `borrow()`. It's easy to
ignore, and ignoring the problem is how the original lent the same book twice.

## Decision 6: rules are separate from printing

`Library` holds the rules and prints nothing. `LendingDesk` prints the original
messages word for word, and `EndOfDayReport` builds the report lines. Because of this:
- the rules can be checked without reading console text;
- the report wording can change without any risk to the lending rules;
- the output can be compared line by line with the original's (see "Behaviour" below).

---

## The coming change: adding e-loans

### In this design: one new file

```java
package com.amalitech.training.readingroom.item;

/** A digital e-loan: 21 days for every member. */
public final class EBook extends LendingItem {

    private static final LoanPeriod LOAN_PERIOD = new LoanPeriod(21, 21);

    public EBook(String title) {
        super(title);
    }

    @Override
    public String typeCode() {
        return "E_LOAN";
    }

    @Override
    public LoanPeriod loanPeriod() {
        return LOAN_PERIOD;
    }
}
```

Use it like any other item: `library.addItem(new EBook("Purple Hibiscus"));`

**What changes:** one new file. **What doesn't change:** `Library`, `Loan`,
`LendingDesk`, `EndOfDayReport`, `Book`, `AudioKit`, `Member`.

I checked this by compiling `EBook` against the existing compiled classes, without
editing any of them. The result:

```
Lent "Purple Hibiscus" to Ama Owusu for 21 days
Cannot lend "Purple Hibiscus" to Kofi Mensah: it is already on loan to Ama Owusu

=== The Reading Room - end of day ===
Items:
  "Purple Hibiscus" (E_LOAN): ON_LOAN - with Ama Owusu
Active loans:
  Loan #1: "Purple Hibiscus" -> Ama Owusu
```

E-loans automatically get the one-loan-at-a-time rule, loan numbering, the report and
overdue tracking, because all of those live in `Library` and `EndOfDayReport` and only
ever talk to `LendingItem`.

### In the original, the same change would have meant

1. **Editing the shared `borrow()` method:** adding a third
   `else if (item.type.equals("E_LOAN"))` branch and copying the status, borrower and
   message lines again. That's the same method whose last edit broke book returns.
2. **Typing `"E_LOAN"` exactly right everywhere.** A typo compiles fine and silently
   creates a 0-day loan.
3. **Living with returns matched by title.** An e-loan usually has the same title as
   the printed book, so returning one could close the other's loan.
4. **Still no protection against lending the same item twice.**

---

## Behaviour: the same as the original, except the rule that must never break

**Original program's output:**
```
Lent "Things Fall Apart" to Kofi Mensah for 14 days
Lent "French Audio Course" to Ama Owusu for 14 days
Returned "French Audio Course"
Lent "Things Fall Apart" to Ama Owusu for 28 days

=== The Reading Room - end of day ===
Items:
  "Things Fall Apart" (BOOK): ON_LOAN - with Ama Owusu
  "French Audio Course" (AUDIO_KIT): AVAILABLE
Active loans:
  Loan #1: "Things Fall Apart" -> Kofi Mensah
  Loan #3: "Things Fall Apart" -> Ama Owusu
```

**Rebuild's output (same afternoon):**
```
Lent "Things Fall Apart" to Kofi Mensah for 14 days
Lent "French Audio Course" to Ama Owusu for 14 days
Returned "French Audio Course"
Cannot lend "Things Fall Apart" to Ama Owusu: it is already on loan to Kofi Mensah

=== The Reading Room - end of day ===
Items:
  "Things Fall Apart" (BOOK): ON_LOAN - with Kofi Mensah
  "French Audio Course" (AUDIO_KIT): AVAILABLE
Active loans:
  Loan #1: "Things Fall Apart" -> Kofi Mensah
```

**What's the same:** the loan periods (book 14/28, audio kit 7/14), the "Lent…" and
"Returned…" messages, loan numbering, and the report format.

**What's different, on purpose:** the second loan of the novel is refused. Because
of that, the report correctly shows the novel with Kofi, and only one active loan.

The original program can be recovered from the first commit to compare:
`git show 37a167b:src/main/java/LendingTracker.java`

**Other deliberate differences.** Each one is a case where the original corrupted its
own records or accepted a mistake:

| Situation | Original | Rebuild |
|---|---|---|
| Returning one copy of a title | closes the loans on **all** copies with that title | closes only that copy's loan |
| Misspelt item type (`"book"`) | silent 0-day loan | can't happen: item kinds are classes |
| Item or member never added to the library | accepted | rejected with an error |
| The same item added twice | listed twice in the report | rejected with an error |

---

## Improvement beyond the brief: overdue loans

**What it does.** Every loan now records the date it started, so it has a due date
(`Loan.dueOn()`). The end-of-day report gains an **Overdue loans** section listing any
loan past its due date:

```
Overdue loans:
  Loan #1: "Things Fall Apart" -> Kofi Mensah (due 2026-11-25, 3 days late)
```

**Why it matters to the library.** The loan period was the main rule the original
calculated, yet it threw the number away, so nobody could tell which items were late.
Late items were only noticed when someone asked for them, and items that never came
back were simply lost stock. For a small volunteer-run library, a daily list of who to
remind, and about what, is the most useful thing the tool can do with data it already
collects.

**How it was kept safe:**
- The section appears **only when something is overdue**. On a normal day the report
  is exactly the same as the original's, so existing behaviour is preserved.
- It reads from the same single record of loans, so it can't disagree with the
  "Active loans" section.
- `EndOfDayReport.lines(library, date)` can produce the report for any date, for
  example to see what will be overdue by Friday.
- It works for e-loans with no extra code.

---

## Changes from the sketch in DIAGNOSIS.md

The design sketch was added to `DIAGNOSIS.md` after my first code change, not before
as the brief asks. The structure I built matches it. Additions made while building,
and why:

- **`Loan` and `Library` share the `library` package**, so `Loan.open` and `close`
  can be package-private and only `Library` can create or close a loan.
- **`isOnLoan()` sits next to `getActiveLoan()`**, so callers check first instead of
  receiving `null`.
- **`dueOn()`, `isOverdueOn()`, `overdueLoans()` and the report's overdue section**
  were added for the improvement above.
- **Packages renamed** from `readingroom` to `com.amalitech.training.readingroom`, to
  follow Java's reversed-domain naming convention and match the project's `groupId`.

## Known limits

- **Data lives in memory** and is lost when the program closes. Saving to a file or
  database is the natural next step.
- **Not built for two desks using one `Library` at the same moment.** That needs
  thread safety, which is outside this module.
- **A new member type** (for example, seniors) means updating `MemberType` and
  `LoanPeriod`, and choosing a period for every item kind. That's a wider change than
  adding an item kind, but the brief only says new *item* kinds are coming.
