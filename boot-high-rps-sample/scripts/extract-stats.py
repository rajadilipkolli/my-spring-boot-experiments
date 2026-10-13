#!/usr/bin/env python3
"""
extract-stats.py: Parse a Gatling 3.13+ HTML report and emit stats.json
compatible with compare-results.ps1.

Usage:
    python3 scripts/extract-stats.py [--report-dir target/gatling/<run>] [--out stats.json]

The script finds the most recent Gatling report under target/gatling/ when
--report-dir is not provided.
"""

import argparse
import json
import os
import re
import sys

# ---------------------------------------------------------------------------
# Column indices emitted by Gatling 3.13+ (1-based class names in the HTML)
# col-2  total requests
# col-3  ok requests
# col-4  ko requests
# col-5  % KO  (unused – we compute from ko/total)
# col-6  mean RPS
# col-7  min response time
# col-8  p50
# col-9  p75
# col-10 p95
# col-11 p99
# col-12 max
# col-13 mean
# col-14 std dev
# ---------------------------------------------------------------------------

COL_TOTAL = "col-2"
COL_OK    = "col-3"
COL_KO    = "col-4"
COL_RPS   = "col-6"
COL_MIN   = "col-7"
COL_P50   = "col-8"
COL_P75   = "col-9"
COL_P95   = "col-10"
COL_P99   = "col-11"
COL_MAX   = "col-12"
COL_MEAN  = "col-13"
COL_STDDEV= "col-14"


def _num(s):
    """Parse a numeric string; return int if whole, float otherwise, or None."""
    s = s.strip()
    if not s:
        return None
    try:
        f = float(s)
        return int(f) if f == int(f) else f
    except ValueError:
        return None


def _parse_row(cells):
    """
    Given a dict of {col-N: value} build a stats sub-object matching the
    shape used by compare-results.ps1.
    """
    total = _num(cells.get(COL_TOTAL, ""))
    ok    = _num(cells.get(COL_OK,    ""))
    ko    = _num(cells.get(COL_KO,    ""))
    rps   = _num(cells.get(COL_RPS,   ""))
    p50   = _num(cells.get(COL_P50,   ""))
    p75   = _num(cells.get(COL_P75,   ""))
    p95   = _num(cells.get(COL_P95,   ""))
    p99   = _num(cells.get(COL_P99,   ""))
    mx    = _num(cells.get(COL_MAX,   ""))
    mn    = _num(cells.get(COL_MIN,   ""))

    return {
        "numberOfRequests": {
            "total": total,
            "ok":    ok,
            "ko":    ko,
        },
        "meanNumberOfRequestsPerSecond": {
            "total": rps,
        },
        "minResponseTime":  {"total": mn},
        "maxResponseTime":  {"total": mx},
        "percentiles1":     {"total": p50},
        "percentiles2":     {"total": p75},
        "percentiles3":     {"total": p95},
        "percentiles4":     {"total": p99},
    }


# Regex to pull out <td class="... col-N ...">VALUE</td> on a single line.
_TD_RE = re.compile(
    r'<td[^>]+class="[^"]*\b(col-\d+)\b[^"]*"[^>]*>\s*([^<]*?)\s*</td>',
    re.IGNORECASE,
)

# Regex to extract the request name from the stats-table span.
_NAME_RE = re.compile(
    r'id="stats-table-[^"]*"\s+class="ellipsed-name"\s*>([^<]+)<',
    re.IGNORECASE,
)


def parse_report(report_dir):
    index_path = os.path.join(report_dir, "index.html")
    if not os.path.isfile(index_path):
        sys.exit(f"ERROR: index.html not found in {report_dir}")

    with open(index_path, encoding="utf-8", errors="replace") as fh:
        lines = fh.readlines()

    result = {"stats": None, "contents": {}}

    # Simple line-by-line state machine:
    #   * When we see a <tr id="..." data-parent="..."> line, start a new row.
    #   * When we see the ROOT row (no data-parent or data-parent absent and
    #     id contains ROOT), it is the global row.
    #   * Accumulate <td col-N> values until </tr>.

    current_name  = None    # human-readable request name
    current_cells = {}      # col-N -> value string
    is_root_row   = False

    def flush():
        nonlocal current_name, current_cells, is_root_row
        if current_cells:
            stats = _parse_row(current_cells)
            if is_root_row:
                result["stats"] = stats
            elif current_name:
                result["contents"][current_name] = {"stats": stats}
        current_name  = None
        current_cells = {}
        is_root_row   = False

    for line in lines:
        # --- start of a new row ---
        tr_match = re.search(r'<tr\s+id="([^"]+)"', line, re.IGNORECASE)
        if tr_match:
            flush()
            row_id = tr_match.group(1)
            is_root_row = (row_id == "ROOT")

        # --- request name ---
        name_match = _NAME_RE.search(line)
        if name_match and current_name is None:
            current_name = name_match.group(1).strip()

        # --- data cells ---
        for td_match in _TD_RE.finditer(line):
            col, val = td_match.group(1), td_match.group(2)
            current_cells[col] = val

    flush()

    return result


def find_latest_report(base_dir="target/gatling"):
    if not os.path.isdir(base_dir):
        sys.exit(f"ERROR: Gatling output directory not found: {base_dir}")
    runs = [
        d for d in os.listdir(base_dir)
        if os.path.isdir(os.path.join(base_dir, d))
        and os.path.isfile(os.path.join(base_dir, d, "index.html"))
    ]
    if not runs:
        sys.exit(f"ERROR: No Gatling report found under {base_dir}")
    latest = sorted(runs)[-1]
    return os.path.join(base_dir, latest)


def main():
    ap = argparse.ArgumentParser(description="Extract Gatling stats to JSON")
    ap.add_argument("--report-dir", default=None,
                    help="Path to a specific Gatling report directory")
    ap.add_argument("--out", default="stats.json",
                    help="Output file path (default: stats.json)")
    args = ap.parse_args()

    report_dir = args.report_dir or find_latest_report()
    print(f"Parsing report: {report_dir}", file=sys.stderr)

    data = parse_report(report_dir)

    if data["stats"] is None:
        sys.exit("ERROR: Could not find global stats row in index.html")

    with open(args.out, "w", encoding="utf-8") as fh:
        json.dump(data, fh, indent=2)

    print(f"Written: {args.out}", file=sys.stderr)
    total = data["stats"]["numberOfRequests"]["total"]
    rps   = data["stats"]["meanNumberOfRequestsPerSecond"]["total"]
    p95   = data["stats"]["percentiles3"]["total"]
    p99   = data["stats"]["percentiles4"]["total"]
    print(f"Global  total={total}  rps={rps}  p95={p95}ms  p99={p99}ms", file=sys.stderr)


if __name__ == "__main__":
    main()
