# The Reading Room — Lending Tracker

A lending tracker for The Reading Room, a small volunteer-run community library. This is
a rebuild of the volunteers' original single-file program: it **behaves exactly as the
original did**, except that **an item can now only ever be on one loan at a time**. The
code is split into small classes by concern, so a volunteer can change one rule without
touching the others, and a new kind of item (such as e-loans) is one new class.

## Documents

- [`DIAGNOSIS.md`](DIAGNOSIS.md): what was wrong with the original program, ranked by risk
  to the library, with the design sketch and class diagram.
- [`DESIGN.md`](DESIGN.md): the design decisions and the alternatives considered, how
  e-loans would be added, the evidence that behaviour is preserved, and the improvement
  beyond the brief (overdue loans).

## Structure

Packages under `src/main/java/com/amalitech/training/readingroom/`:

| Package | What it holds |
|---|---|
| `item` | `LendingItem` (abstract), `Book`, `AudioKit`, `LoanPeriod`: each kind of item owns its loan rule |
| `member` | `Member`, `MemberType` |
| `library` | `Library` (the only place loans are opened and closed), `Loan`, `ItemAlreadyOnLoanException` |
| `desk` | `LendingDesk`: the volunteer-facing actions and messages |
| `report` | `EndOfDayReport`: builds and prints the end-of-day report |
| (root) | `LendingTracker`: entry point that replays the original program's afternoon |

## Design notes

The full reasoning, with alternatives and evidence, is in [`DESIGN.md`](DESIGN.md). In short:

**1. One place answers "who has this item?"** The original stored it twice (on the item and
in the loan list), and the two drifted apart. `Library` now keeps a single map from each item
to its active loan. `borrow()` checks it first and throws `ItemAlreadyOnLoanException` if the
item is out; returns close only that item's loan. Item status is worked out from the map,
never stored, so the report can't contradict itself.

**2. Each kind of item owns its loan rule.** `LendingItem` is an abstract class; `Book` and
`AudioKit` each supply their own `LoanPeriod`. `Library` never checks which kind of item it
has. *Alternative considered:* an `ItemType` enum (simpler, all rules in one file). I chose
subclasses because every new kind would otherwise edit that shared file, and if e-loans
need different *behaviour* the enum turns into `if (type == …)` checks: the pattern that
made the original fragile.

**3. Nothing outside `Library` can make the records invalid.** Private final fields, no
setters, validating constructors, read-only lists (`List.copyOf`), and only `Library` can
open or close a loan. A refused loan is a checked exception, so no caller can ignore it.

**4. Rules are separate from printing.** `Library` holds the rules; `LendingDesk` and
`EndOfDayReport` produce exactly the original program's messages and report.

### How e-loans slot in

Adding e-loans is **one new class**, and nothing else changes:

```java
public final class EBook extends LendingItem {
    private static final LoanPeriod LOAN_PERIOD = new LoanPeriod(21, 21);
    public EBook(String copyId, String title) { super(copyId, title); }
    @Override public String typeCode() { return "E_LOAN"; }
    @Override public LoanPeriod loanPeriod() { return LOAN_PERIOD; }
}
```

`Library`, `Loan`, `LendingDesk`, `EndOfDayReport`, `Book` and `AudioKit` are untouched, and
e-loans automatically get the one-loan rule, loan numbering, the report and overdue tracking.
This was checked by compiling `EBook` against the existing classes without editing them.

### What the old version would have forced you to touch

- The shared `borrow()` method: a third copy-pasted `else if (item.type.equals("E_LOAN"))`
  branch, in the same method whose last edit broke book returns.
- The string `"E_LOAN"` everywhere it is used: a typo compiles and silently creates a 0-day loan.
- `returnItem()`, which matches loans by title: an e-loan usually shares its title with the
  printed book, so one return could close the other's loan.
- And still nothing would stop an item being lent twice.

### Improvement beyond the brief

Loans record their start date, so they have due dates, and the end-of-day report adds an
**Overdue loans** section when something is late. Late items are otherwise only noticed when
someone asks for them, and items that never come back are lost stock. On a normal day the
report is unchanged from the original's.

## Running it

You need JDK 21 and Maven. The program itself has no external dependencies; JUnit is
used only by the tests. The Maven build compiles main and test code with
`-Xlint:all -Werror`, so any compiler warning fails the build.

**With Maven:**

```bash
mvn compile
mvn exec:java -Dexec.mainClass=com.amalitech.training.readingroom.LendingTracker
```

**Or directly with the JDK** (macOS/Linux/Git Bash):

```bash
javac -Xlint:all -Werror --release 21 -d target/classes $(find src/main/java -name "*.java")
java -cp target/classes com.amalitech.training.readingroom.LendingTracker
```

## Running the tests

```bash
mvn test
```

The tests (in `src/test/java`) cover the one-loan-per-item rule, returns, the loan-period
table, copy IDs and member IDs, the end-of-day report and overdue section, and the exact
console output the volunteer sees. See [`DESIGN.md`](DESIGN.md) for how they show
behaviour is preserved.

## Expected output

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

Every line matches the original program except the "Cannot lend…" line: the original lent
the novel a second time and its report then showed two active loans for one book.

Every item has a copy ID (its barcode, such as `B-0001`) and every member has a member ID
(their card number, such as `M-0001`). The IDs decide which copy and which person a loan
belongs to, so two copies of a title or two members with the same name are never confused.
They are not printed, so the output stays the same as the original program's.

When a loan is past its due date, the report also ends with an **Overdue loans** section.
