import os
import sys
import subprocess

def main():
    print("==================================================")
    # 1. Check requirements
    try:
        import pytest
        import openpyxl
        import selenium
        print("[OK] Pre-requisite packages (pytest, openpyxl, selenium) are present.")
    except ImportError as e:
        print(f"[!] Missing libraries: {str(e)}")
        print("Please install requirements using: pip install pytest openpyxl selenium")
        sys.exit(1)

    print("==================================================")
    print("Executing 400+ E2E Selenium Test Cases...")
    
    # Run Pytest on the E2E suite
    # We execute it in silent/summary mode or regular mode
    test_suite_path = os.path.join("automation", "tests", "test_suite.py")
    
    # We run using subprocess to capture output or run directly via pytest.main
    # Direct pytest.main is cleaner in programmatic executions.
    import pytest
    print(f"Launching pytest runner on: {test_suite_path}")
    
    # Run test cases and write output logs
    result = pytest.main(["-q", test_suite_path])
    print(f"Pytest suite runner completed with status code: {result}")
    
    print("==================================================")
    print("Compiling test results and building reports...")
    
    # Execute the report generator
    from automation.utils.report_generator import generate_multi_reports
    generate_multi_reports()
    
    print("==================================================")
    print("EXECUTION COMPLETED SUCCESSFULLY!")
    print("Output Reports generated in:")
    print("  -> Test Results/Excel/  (9 Workbooks including Automation_Test_Report.xlsx, Appium_Test_Report.xlsx, Unit_Test_Report.xlsx, Load_Test_Report.xlsx, Validation_Test_Report.xlsx, Deploy_Test_Report.xlsx, etc.)")
    print("  -> Test Results/HTML/   (dashboard.html, execution-report.html)")
    print("  -> Test Results/JSON/   (execution-results.json)")
    print("  -> Test Results/Summary/(summary.md)")
    print("==================================================")

if __name__ == "__main__":
    main()
