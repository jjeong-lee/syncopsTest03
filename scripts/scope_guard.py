from pathlib import Path
excluded = ['첨부파일 관리','Excel 관리','개인정보 관리','접속기록 관리','감사로그 관리','배치작업 관리','평가대상자','점수산출 업무']
roots = [Path('backend'), Path('frontend'), Path('infra')]
hits = []
for root in roots:
    if not root.exists():
        continue
    for path in root.rglob('*'):
        if path.is_file() and path.suffix.lower() in {'.java','.sql','.ts','.tsx','.js','.jsx','.yaml','.yml','.md'}:
            text = path.read_text(errors='ignore')
            for word in excluded:
                idx = text.find(word)
                if idx >= 0:
                    context = text[max(0, idx-80):idx+120]
                    if '제외' not in context and '후속' not in context:
                        hits.append((str(path), word))
if hits:
    raise SystemExit(f'Excluded features promoted into implementation: {hits[:20]}')
print('OK excluded features are not promoted into implementation scope')
