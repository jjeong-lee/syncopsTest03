
import json
from pathlib import Path
contract = json.loads(Path('docs/implementation/execution-persistence-contract.json').read_text(encoding='utf-8'))
assert contract['decision_status'] == 'clarification_required'
assert contract['persistence_technology'] == 'mybatis'
assert contract['access_mode'] == 'blocking'
assert contract['conflicts'] == ['MULTIPLE_EXECUTION_MODELS']
print('OK execution/persistence marker preserved; handoff is NOT build-ready')
